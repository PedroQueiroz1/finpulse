package com.finpulse.auth.config;

import com.finpulse.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*
Os endpoints "protegidos" não estão realmente protegidos, qualquer pessoa acessa. 
Precisamos de um filtro que intercepta toda requisição, verifica se tem um token JWT válido no header, e autentica o usuário no contexto do Spring Security.

Request chega → passa pelo JwtAuthenticationFilter
Tem header Authorization: Bearer xxx? Se não → passa direto (endpoint pode ser público)
Se tem → extrai o token → valida assinatura e expiração
Se válido → coloca o usuário no SecurityContext → Spring Security sabe quem é
Se inválido → não autentica → se o endpoint exigir auth, Spring retorna 401

Metodo feito por Pedro Queiroz
Projeto de ESTUDOS
*/
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Extrai o header Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. Se não tem header ou não começa com "Bearer ", passa direto
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Remove o "Bearer " para pegar só o token
        String token = authHeader.substring(7);

        try {
            // 4. Extrai o e-mail do token
            String email = jwtService.extractEmail(token);

            // 5. Se tem e-mail e ainda não está autenticado neste request
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Valida o token
                if (jwtService.isTokenValid(token, email)) {

                    // 7. Extrai a role do token para definir as authorities
                    String role = jwtService.extractClaim(token, claims ->
                            claims.get("role", String.class));

                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role)
                    );

                    // 8. Cria o objeto de autenticação e coloca no contexto
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Usuário autenticado via JWT: {}", email);
                }
            }
        } catch (Exception e) {
            log.warn("Falha ao processar JWT: {}", e.getMessage());
            // Não lança exceção — apenas não autentica. O Spring Security cuida do 401.
        }

        // 9. Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }
}