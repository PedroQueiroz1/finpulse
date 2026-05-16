package com.finpulse.stock.resilience;

import com.finpulse.stock.AbstractIntegrationTest;
import com.finpulse.stock.dto.StockQuoteResponse;
import com.finpulse.stock.exception.ExternalApiException;
import com.finpulse.stock.provider.AlphaVantageProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Circuit Breaker - Testes de Integração")
class CircuitBreakerIntegrationTest extends AbstractIntegrationTest {

    private static final String AV_SUCCESS_BODY =
            "{\"Global Quote\":{\"01. symbol\":\"AAPL\",\"02. open\":\"175.50\"," +
            "\"03. high\":\"178.90\",\"04. low\":\"174.20\",\"05. price\":\"177.30\"," +
            "\"06. volume\":\"45000000\",\"08. previous close\":\"176.00\"," +
            "\"09. change\":\"1.30\",\"10. change percent\":\"0.7386%\"}}";

    @Autowired
    private AlphaVantageProvider provider;

    @Autowired
    private CircuitBreakerRegistry cbRegistry;

    private CircuitBreaker cb;

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
        cb = cbRegistry.circuitBreaker("alpha-vantage");
        cb.reset();
    }

    @Test
    @DisplayName("deve estar FECHADO inicialmente")
    void deveCircuitBreakerEstarFechadoInicialmente() {
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("deve abrir após falhas consecutivas acima do threshold")
    void deveAbrirAposFalhasConsecutivas() {
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .willReturn(aResponse().withStatus(503)));

        // minimum-number-of-calls=3, failure-rate-threshold=50%
        // 3 falhas = 100% → OPEN
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> provider.getQuote("AAPL"))
                    .isInstanceOf(ExternalApiException.class);
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("não deve permitir chamadas quando ABERTO")
    void naoDevePermitirChamadasQuandoAberto() {
        cb.transitionToOpenState();

        assertThatThrownBy(() -> provider.getQuote("AAPL"))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("indisponível");

        // CB em OPEN não deve repassar chamada ao provider externo
        wireMock.verify(0, getRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("deve transicionar para HALF_OPEN após o wait-duration")
    void deveTransicionarParaHalfOpenAposEspera() throws InterruptedException {
        cb.transitionToOpenState();
        // wait-duration-in-open-state = 1s em application-test.yml
        // automatic-transition-from-open-to-half-open-enabled = true
        Thread.sleep(1500);

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
    }

    @Test
    @DisplayName("deve fechar novamente após chamadas bem-sucedidas no HALF_OPEN")
    void deveFecharNovamenteAposChamadasBemSucedidas() {
        cb.transitionToOpenState();
        cb.transitionToHalfOpenState();

        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(AV_SUCCESS_BODY)));

        // permitted-number-of-calls-in-half-open-state = 2
        for (int i = 0; i < 2; i++) {
            StockQuoteResponse result = provider.getQuote("AAPL");
            assertThat(result).isNotNull();
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
