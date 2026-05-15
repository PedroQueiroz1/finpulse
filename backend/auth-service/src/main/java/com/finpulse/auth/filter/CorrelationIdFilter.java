package com.finpulse.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter que gerencia o Correlation ID para rastreamento distribuído.
 *
 * Responsabilidades:
 * 1. Receber o correlation ID do header (se a requisição já veio com um, vinda de outro serviço)
 * 2. Gerar um novo UUID se não veio (primeira entrada no sistema)
 * 3. Adicionar ao MDC para os logs capturarem
 * 4. Devolver o ID no header da resposta (cliente pode usar pra debugar)
 * 5. Limpar o MDC ao fim (CRÍTICO: evita vazamento entre requisições)
 *
 * @Order(HIGHEST_PRECEDENCE): roda antes de qualquer outro filter (inclusive o JWT).
 * Garantia: TODOS os logs (até os de autenticação) terão o correlation ID.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Verifica se já veio um correlation ID no header
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        // 2. Se não veio, gera um novo (primeira entrada no sistema)
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        try {
            // 3. Adiciona ao MDC — daqui em diante, todos os logs terão acesso
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

            // 4. Devolve no header da resposta
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // 5. Continua a cadeia de filters
            filterChain.doFilter(request, response);

        } finally {
            // 6. CRÍTICO: limpa o MDC pra não vazar pra próxima requisição
            // Threads do Tomcat são reutilizadas via pool, então se não limpar,
            // a requisição da Maria pode ganhar o ID do Pedro!
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}