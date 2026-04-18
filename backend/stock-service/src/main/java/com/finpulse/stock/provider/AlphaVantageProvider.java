package com.finpulse.stock.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.finpulse.stock.dto.CompanyInfoResponse;
import com.finpulse.stock.dto.StockQuoteResponse;
import com.finpulse.stock.exception.ExternalApiException;
import com.finpulse.stock.exception.StockNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Adapter Pattern — adapta a resposta do Alpha Vantage
 * (com chaves esquisitas tipo "01. symbol") para nosso DTO padronizado.
 */
@Component
public class AlphaVantageProvider implements StockProvider {

    private static final Logger log = LoggerFactory.getLogger(AlphaVantageProvider.class);
    private static final String PROVIDER_NAME = "alpha-vantage";

    private final WebClient webClient;
    private final String apiKey;

    public AlphaVantageProvider(
            @Value("${stock.providers.alpha-vantage.base-url}") String baseUrl,
            @Value("${stock.providers.alpha-vantage.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    @Override
    public StockQuoteResponse getQuote(String symbol) {
        log.info("[Alpha Vantage] Buscando cotação: {}", symbol);
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/query")
                            .queryParam("function", "GLOBAL_QUOTE")
                            .queryParam("symbol", symbol)
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null) {
                throw new ExternalApiException(PROVIDER_NAME, "Resposta vazia");
            }

            // Alpha Vantage retorna erro dentro do JSON (não como HTTP 4xx/5xx)
            if (response.has("Error Message") || response.has("Information")) {
                String errorMsg = response.has("Error Message")
                        ? response.get("Error Message").asText()
                        : response.get("Information").asText();
                if (errorMsg.toLowerCase().contains("invalid api call")) {
                    throw new StockNotFoundException(symbol);
                }
                throw new ExternalApiException(PROVIDER_NAME, errorMsg);
            }

            JsonNode quote = response.get("Global Quote");
            if (quote == null || quote.isEmpty()) {
                throw new StockNotFoundException(symbol);
            }

            return adaptQuote(quote);

        } catch (WebClientResponseException e) {
            log.error("[Alpha Vantage] HTTP {}: {}", e.getStatusCode(), e.getMessage());
            throw new ExternalApiException(PROVIDER_NAME,
                    "Falha HTTP " + e.getStatusCode(), e);
        }
    }

    @Override
    public CompanyInfoResponse getCompanyInfo(String symbol) {
        log.info("[Alpha Vantage] Buscando dados da empresa: {}", symbol);
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/query")
                            .queryParam("function", "OVERVIEW")
                            .queryParam("symbol", symbol)
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || response.isEmpty() || !response.has("Symbol")) {
                throw new StockNotFoundException(symbol);
            }

            return adaptCompanyInfo(response);

        } catch (WebClientResponseException e) {
            throw new ExternalApiException(PROVIDER_NAME,
                    "Falha HTTP " + e.getStatusCode(), e);
        }
    }

    // ============================================================
    // Métodos de adaptação (Adapter Pattern)
    // ============================================================

    private StockQuoteResponse adaptQuote(JsonNode quote) {
        return new StockQuoteResponse(
                getText(quote, "01. symbol"),
                getBigDecimal(quote, "05. price"),
                getBigDecimal(quote, "09. change"),
                parseChangePercent(quote),
                getBigDecimal(quote, "03. high"),
                getBigDecimal(quote, "04. low"),
                getBigDecimal(quote, "02. open"),
                getBigDecimal(quote, "08. previous close"),
                getLong(quote, "06. volume"),
                PROVIDER_NAME,
                LocalDateTime.now()
        );
    }

    private CompanyInfoResponse adaptCompanyInfo(JsonNode node) {
        return new CompanyInfoResponse(
                getText(node, "Symbol"),
                getText(node, "Name"),
                getText(node, "Description"),
                getText(node, "Exchange"),
                getText(node, "Country"),
                getText(node, "Sector"),
                getText(node, "Industry"),
                getText(node, "Currency"),
                getText(node, "OfficialSite"),
                null,  // Alpha Vantage não fornece logo
                PROVIDER_NAME
        );
    }

    // ============================================================
    // Helpers para extrair campos do JSON com segurança
    // ============================================================

    private String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value == null || value.isNull()) ? null : value.asText();
    }

    private BigDecimal getBigDecimal(JsonNode node, String field) {
        String text = getText(node, field);
        if (text == null || text.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private Long getLong(JsonNode node, String field) {
        String text = getText(node, field);
        if (text == null || text.isBlank()) return 0L;
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private BigDecimal parseChangePercent(JsonNode quote) {
        // Alpha Vantage retorna algo como "1.2345%" — precisa remover o %
        String text = getText(quote, "10. change percent");
        if (text == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(text.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}