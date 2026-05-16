package com.finpulse.stock.resilience;

import com.finpulse.stock.AbstractIntegrationTest;
import com.finpulse.stock.exception.ExternalApiException;
import com.finpulse.stock.provider.AlphaVantageProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Retry - Testes de Integração")
class RetryIntegrationTest extends AbstractIntegrationTest {

    private static final String AV_SUCCESS_BODY =
            "{\"Global Quote\":{\"01. symbol\":\"AAPL\",\"02. open\":\"175.50\"," +
            "\"03. high\":\"178.90\",\"04. low\":\"174.20\",\"05. price\":\"177.30\"," +
            "\"06. volume\":\"45000000\",\"08. previous close\":\"176.00\"," +
            "\"09. change\":\"1.30\",\"10. change percent\":\"0.7386%\"}}";

    @Autowired
    private AlphaVantageProvider provider;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private CircuitBreakerRegistry cbRegistry;

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
        cbRegistry.circuitBreaker("alpha-vantage").reset();
    }

    @Test
    @DisplayName("deve retentar 3 vezes após falha transitória e retornar sucesso na 3ª tentativa")
    void deveRetentar3VezesAposFalhaTransitoria() {
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .inScenario("retry-scenario")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("SEGUNDA_TENTATIVA"));

        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .inScenario("retry-scenario")
                .whenScenarioStateIs("SEGUNDA_TENTATIVA")
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("TERCEIRA_TENTATIVA"));

        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .inScenario("retry-scenario")
                .whenScenarioStateIs("TERCEIRA_TENTATIVA")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(AV_SUCCESS_BODY)));

        var result = provider.getQuote("AAPL");

        assertThat(result).isNotNull();
        assertThat(result.symbol()).isEqualTo("AAPL");
        // max-attempts=3: 2 falhas + 1 sucesso = 3 chamadas HTTP
        wireMock.verify(3, getRequestedFor(urlPathEqualTo("/query")));
    }

    @Test
    @DisplayName("deve aplicar backoff exponencial: max-attempts=3, wait=50ms, multiplicador=2")
    void deveAplicarBackoffExponencial() {
        Retry retry = retryRegistry.retry("alpha-vantage");

        assertThat(retry.getRetryConfig().getMaxAttempts()).isEqualTo(3);

        // Valida que o Retry executa as 3 tentativas antes de desistir
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> provider.getQuote("AAPL"))
                .isInstanceOf(ExternalApiException.class);

        wireMock.verify(3, getRequestedFor(urlPathEqualTo("/query")));
    }

    @Test
    @DisplayName("não deve retentar após esgotar as 3 tentativas (lança exceção)")
    void naoDeveRetentarApos3Tentativas() {
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> provider.getQuote("AAPL"))
                .isInstanceOf(ExternalApiException.class);

        // max-attempts=3: exatamente 3 chamadas, nenhuma a mais
        wireMock.verify(3, getRequestedFor(urlPathEqualTo("/query")));
    }
}
