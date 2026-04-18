package com.finpulse.stock.provider;

import com.finpulse.stock.dto.CompanyInfoResponse;
import com.finpulse.stock.dto.StockQuoteResponse;
import com.finpulse.stock.exception.ExternalApiException;
import com.finpulse.stock.exception.ProviderNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Facade Pattern — ponto único de entrada para os providers.
 * Esconde do service a complexidade de:
 *   - escolher o provider (Strategy)
 *   - fazer fallback para outro provider em caso de falha
 *   - validar qual provider é válido
 */
@Component
public class StockProviderFacade {

    private static final Logger log = LoggerFactory.getLogger(StockProviderFacade.class);

    private final Map<String, StockProvider> providers;
    private final String defaultProviderName;

    /**
     * O Spring injeta AUTOMATICAMENTE todos os beans que implementam StockProvider.
     * Se amanhã criarmos YahooFinanceProvider, ele cai aqui de graça.
     */
    public StockProviderFacade(
            List<StockProvider> providerList,
            @Value("${stock.default-provider}") String defaultProviderName) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(StockProvider::getName, Function.identity()));
        this.defaultProviderName = defaultProviderName;
        log.info("Providers carregados: {}", providers.keySet());
        log.info("Provider padrão: {}", defaultProviderName);
    }

    /** Retorna a cotação usando o provider padrão (com fallback automático). */
    public StockQuoteResponse getQuote(String symbol) {
        return getQuote(symbol, defaultProviderName);
    }

    /** Retorna a cotação usando um provider específico, com fallback para os outros. */
    public StockQuoteResponse getQuote(String symbol, String preferredProvider) {
        return executeWithFallback(preferredProvider,
                provider -> provider.getQuote(symbol),
                "cotação de " + symbol);
    }

    public CompanyInfoResponse getCompanyInfo(String symbol) {
        return getCompanyInfo(symbol, defaultProviderName);
    }

    public CompanyInfoResponse getCompanyInfo(String symbol, String preferredProvider) {
        return executeWithFallback(preferredProvider,
                provider -> provider.getCompanyInfo(symbol),
                "empresa " + symbol);
    }

    public List<String> getAvailableProviders() {
        return providers.keySet().stream().sorted().toList();
    }

    // ============================================================
    // Lógica de execução com fallback
    // ============================================================

    private <T> T executeWithFallback(
            String preferredName,
            Function<StockProvider, T> operation,
            String operationDescription) {

        StockProvider preferred = providers.get(preferredName);
        if (preferred == null) {
            throw new ProviderNotAvailableException(preferredName);
        }

        // Tenta primeiro o provider preferido
        try {
            return operation.apply(preferred);
        } catch (ExternalApiException e) {
            log.warn("Provider [{}] falhou para {}: {}. Tentando fallback...",
                    preferredName, operationDescription, e.getMessage());
        }

        // Fallback: tenta os outros providers disponíveis
        for (StockProvider fallback : providers.values()) {
            if (fallback.getName().equals(preferredName)) continue;
            try {
                log.info("Tentando fallback com [{}]", fallback.getName());
                return operation.apply(fallback);
            } catch (ExternalApiException e) {
                log.warn("Fallback [{}] também falhou: {}",
                        fallback.getName(), e.getMessage());
            }
        }

        throw new ExternalApiException("all",
                "Todos os provedores falharam para " + operationDescription);
    }
}