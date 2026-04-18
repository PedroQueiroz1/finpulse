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
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Adapter para a API do Finnhub.
 * Cada campo do JSON deles é uma letra (c = current, h = high, l = low, o = open, etc).
 */
@Component
public class FinnhubProvider implements StockProvider {

    private static final Logger log = LoggerFactory.getLogger(FinnhubProvider.class);
    private static final String PROVIDER_NAME = "finnhub";

    private final WebClient webClient;
    private final String apiKey;

    public FinnhubProvider(
            @Value("${stock.providers.finnhub.base-url}") String baseUrl,
            @Value("${stock.providers.finnhub.api-key}") String apiKey) {
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
        log.info("[Finnhub] Buscando cotação: {}", symbol);
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/quote")
                            .queryParam("symbol", symbol)
                            .queryParam("token", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null) {
                throw new ExternalApiException(PROVIDER_NAME, "Resposta vazia");
            }

            // Finnhub retorna todos os campos como 0 quando o ticker não existe
            BigDecimal currentPrice = getBigDecimal(response, "c");
            if (currentPrice.compareTo(BigDecimal.ZERO) == 0) {
                throw new StockNotFoundException(symbol);
            }

            return adaptQuote(symbol, response);

        } catch (WebClientResponseException e) {
            log.error("[Finnhub] HTTP {}: {}", e.getStatusCode(), e.getMessage());
            throw new ExternalApiException(PROVIDER_NAME,
                    "Falha HTTP " + e.getStatusCode(), e);
        }
    }

    @Override
    public CompanyInfoResponse getCompanyInfo(String symbol) {
        log.info("[Finnhub] Buscando dados da empresa: {}", symbol);
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/stock/profile2")
                            .queryParam("symbol", symbol)
                            .queryParam("token", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || response.isEmpty() || !response.has("ticker")) {
                throw new StockNotFoundException(symbol);
            }

            return adaptCompanyInfo(response);

        } catch (WebClientResponseException e) {
            throw new ExternalApiException(PROVIDER_NAME,
                    "Falha HTTP " + e.getStatusCode(), e);
        }
    }

    // ============================================================
    // Adaptação
    // ============================================================

    private StockQuoteResponse adaptQuote(String symbol, JsonNode data) {
        BigDecimal current = getBigDecimal(data, "c");
        BigDecimal previousClose = getBigDecimal(data, "pc");
        BigDecimal change = current.subtract(previousClose);
        BigDecimal changePercent = previousClose.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : change.divide(previousClose, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

        return new StockQuoteResponse(
                symbol,
                current,
                change,
                changePercent,
                getBigDecimal(data, "h"),
                getBigDecimal(data, "l"),
                getBigDecimal(data, "o"),
                previousClose,
                0L,  // Finnhub /quote não retorna volume
                PROVIDER_NAME,
                LocalDateTime.now()
        );
    }

    private CompanyInfoResponse adaptCompanyInfo(JsonNode data) {
        return new CompanyInfoResponse(
                getText(data, "ticker"),
                getText(data, "name"),
                null,  // Finnhub não retorna descrição no free tier
                getText(data, "exchange"),
                getText(data, "country"),
                null,  // Setor vem em endpoint separado
                getText(data, "finnhubIndustry"),
                getText(data, "currency"),
                getText(data, "weburl"),
                getText(data, "logo"),
                PROVIDER_NAME
        );
    }

    // Helpers
    private String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value == null || value.isNull()) ? null : value.asText();
    }

    private BigDecimal getBigDecimal(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) return BigDecimal.ZERO;
        return BigDecimal.valueOf(value.asDouble());
    }
}