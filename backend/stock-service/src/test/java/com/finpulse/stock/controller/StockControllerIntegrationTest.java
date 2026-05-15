package com.finpulse.stock.controller;

import com.finpulse.stock.AbstractIntegrationTest;
import com.finpulse.stock.helper.StockApiStubs;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.CacheManager;
import org.springframework.http.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StockController - Testes de Integração")
class StockControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CacheManager cacheManager;

    private StockApiStubs stubs;

    @BeforeEach
    void setUp() {
        stubs = new StockApiStubs(wireMock);
        stubs.reset();
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        });
    }

    // ================================================================
    // Cotações
    // ================================================================

    @Nested
    @DisplayName("GET /{symbol}/quote - Cotação")
    class CotacaoPorSimbolo {

        @Test
        @DisplayName("deve retornar cotação AAPL via provider padrão")
        void deveRetornarCotacaoAapl() {
            stubs.stubAlphaVantageQuoteSuccess("AAPL");

            ResponseEntity<String> resp = restTemplate.getForEntity("/api/stocks/AAPL/quote", String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).contains("AAPL").contains("177.30");
        }

        @Test
        @DisplayName("segunda chamada deve usar cache Redis sem chamar WireMock")
        void deveCacheACotacao() {
            stubs.stubAlphaVantageQuoteSuccess("AAPL");

            restTemplate.getForEntity("/api/stocks/AAPL/quote", String.class);
            restTemplate.getForEntity("/api/stocks/AAPL/quote", String.class);

            wireMock.verify(1, getRequestedFor(urlPathEqualTo("/query"))
                    .withQueryParam("function", equalTo("GLOBAL_QUOTE")));
        }

        @Test
        @DisplayName("deve retornar 404 para símbolo inexistente")
        void deveRetornar404ParaSimboloInvalido() {
            stubs.stubAlphaVantageEmptyQuote("ZZZZ");
            stubs.stubFinnhubQuoteNotFound("ZZZZ");

            ResponseEntity<String> resp = restTemplate.getForEntity("/api/stocks/ZZZZ/quote", String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // ================================================================
    // Cotação com provider específico
    // ================================================================

    @Nested
    @DisplayName("GET /{symbol}/quote/{provider}")
    class CotacaoComProvider {

        @Test
        @DisplayName("deve retornar cotação via Finnhub quando solicitado")
        void deveRetornarCotacaoViaFinnhub() {
            stubs.stubFinnhubQuoteSuccess("AAPL");

            ResponseEntity<String> resp = restTemplate.getForEntity(
                    "/api/stocks/AAPL/quote/finnhub", String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).contains("finnhub");
        }

        @Test
        @DisplayName("deve retornar 503 para provider inexistente")
        void deveRetornar503ParaProviderInvalido() {
            ResponseEntity<String> resp = restTemplate.getForEntity(
                    "/api/stocks/AAPL/quote/provider-invalido", String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    // ================================================================
    // Dados da empresa
    // ================================================================

    @Nested
    @DisplayName("GET /{symbol}/company")
    class DadosEmpresa {

        @Test
        @DisplayName("deve retornar dados da empresa AAPL")
        void deveRetornarDadosEmpresa() {
            stubs.stubAlphaVantageCompanySuccess("AAPL");

            ResponseEntity<String> resp = restTemplate.getForEntity(
                    "/api/stocks/AAPL/company", String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).contains("Apple Inc");
        }
    }

    // ================================================================
    // Cache eviction
    // ================================================================

    @Nested
    @DisplayName("DELETE /{symbol}/cache")
    class CacheEviction {

        @Test
        @DisplayName("deve retornar 204 ao evictar cache")
        void deveEvictarCache() {
            ResponseEntity<Void> resp = restTemplate.exchange(
                    "/api/stocks/AAPL/cache", HttpMethod.DELETE, null, Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        @DisplayName("após eviction deve buscar do provider novamente")
        void aposEvicaoDeveBuscarDoProvider() {
            stubs.stubAlphaVantageQuoteSuccess("AAPL");

            restTemplate.getForEntity("/api/stocks/AAPL/quote", String.class);
            restTemplate.exchange("/api/stocks/AAPL/cache", HttpMethod.DELETE, null, Void.class);
            restTemplate.getForEntity("/api/stocks/AAPL/quote", String.class);

            wireMock.verify(2, getRequestedFor(urlPathEqualTo("/query"))
                    .withQueryParam("function", equalTo("GLOBAL_QUOTE")));
        }
    }

    // ================================================================
    // Providers
    // ================================================================

    @Nested
    @DisplayName("GET /providers")
    class ListarProviders {

        @Test
        @DisplayName("deve listar providers disponíveis")
        void deveListarProviders() {
            ResponseEntity<String> resp = restTemplate.getForEntity("/api/stocks/providers", String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).contains("alpha-vantage").contains("finnhub");
        }
    }

    // ================================================================
    // Health endpoint do controller
    // ================================================================

    @Nested
    @DisplayName("GET /health")
    class HealthController {

        @Test
        @DisplayName("deve retornar 200 com mensagem de status")
        void deveRetornarHealth() {
            ResponseEntity<String> resp = restTemplate.getForEntity("/api/stocks/health", String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).contains("Stock Service");
        }
    }

    // ================================================================
    // Actuator
    // ================================================================

    @Nested
    @DisplayName("Actuator endpoints")
    class Actuator {

        @Test
        @DisplayName("deve retornar health UP com Redis")
        void deveRetornarHealthUpComRedis() {
            ResponseEntity<String> resp = restTemplate.getForEntity("/actuator/health", String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).contains("UP");
        }

        @Test
        @DisplayName("endpoint prometheus deve retornar 200")
        void deveRetornarPrometheus() {
            ResponseEntity<String> resp = restTemplate.getForEntity("/actuator/prometheus", String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("deve retornar info com metadados")
        void deveRetornarInfo() {
            ResponseEntity<String> resp = restTemplate.getForEntity("/actuator/info", String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    // ================================================================
    // Correlation ID
    // ================================================================

    @Nested
    @DisplayName("Correlation ID")
    class CorrelationId {

        @Test
        @DisplayName("deve gerar Correlation ID quando header está ausente")
        void deveGerarCorrelationIdQuandoAusente() {
            ResponseEntity<String> resp = restTemplate.getForEntity("/actuator/health", String.class);

            assertThat(resp.getHeaders().getFirst("X-Correlation-ID")).isNotBlank();
        }

        @Test
        @DisplayName("deve reutilizar Correlation ID enviado pelo cliente")
        void deveReutilizarCorrelationIdEnviado() {
            String meuId = "meu-correlation-id-teste-123";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Correlation-ID", meuId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    "/actuator/health", HttpMethod.GET, entity, String.class);

            assertThat(resp.getHeaders().getFirst("X-Correlation-ID")).isEqualTo(meuId);
        }

        @Test
        @DisplayName("deve retornar Correlation ID em qualquer resposta")
        void deveRetornarCorrelationIdNaResposta() {
            stubs.stubAlphaVantageQuoteSuccess("AAPL");

            ResponseEntity<String> resp = restTemplate.getForEntity("/api/stocks/AAPL/quote", String.class);

            assertThat(resp.getHeaders().getFirst("X-Correlation-ID")).isNotBlank();
        }
    }
}
