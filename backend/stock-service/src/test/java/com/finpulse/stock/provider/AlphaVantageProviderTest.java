package com.finpulse.stock.provider;

import com.finpulse.stock.dto.CompanyInfoResponse;
import com.finpulse.stock.dto.StockQuoteResponse;
import com.finpulse.stock.exception.ExternalApiException;
import com.finpulse.stock.exception.StockNotFoundException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AlphaVantageProvider - Testes Unitários")
class AlphaVantageProviderTest {

    static WireMockServer wireMock;
    static AlphaVantageProvider provider;

    @BeforeAll
    static void setUpClass() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        provider = new AlphaVantageProvider(
                "http://localhost:" + wireMock.port(), "test-key");
    }

    @AfterAll
    static void tearDownClass() {
        wireMock.stop();
    }

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @Test
    @DisplayName("deve retornar cotação com dados corretos")
    void deveRetornarCotacaoCorreta() {
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("GLOBAL_QUOTE"))
                .withQueryParam("symbol", equalTo("AAPL"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"Global Quote\":{\"01. symbol\":\"AAPL\",\"02. open\":\"175.50\"," +
                                "\"03. high\":\"178.90\",\"04. low\":\"174.20\",\"05. price\":\"177.30\"," +
                                "\"06. volume\":\"45000000\",\"08. previous close\":\"176.00\"," +
                                "\"09. change\":\"1.30\",\"10. change percent\":\"0.7386%\"}}")));

        StockQuoteResponse result = provider.getQuote("AAPL");

        assertThat(result.symbol()).isEqualTo("AAPL");
        assertThat(result.price()).isEqualByComparingTo(new BigDecimal("177.30"));
        assertThat(result.provider()).isEqualTo("alpha-vantage");
        assertThat(result.volume()).isEqualTo(45_000_000L);
    }

    @Test
    @DisplayName("deve lançar StockNotFoundException quando Global Quote está vazio")
    void deveLancarStockNotFoundParaRespostaVazia() {
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("GLOBAL_QUOTE"))
                .withQueryParam("symbol", equalTo("INVALID"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"Global Quote\":{}}")));

        assertThatThrownBy(() -> provider.getQuote("INVALID"))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    @DisplayName("deve lançar ExternalApiException quando retorna Information (rate limit)")
    void deveLancarExternalApiExceptionParaInformation() {
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("GLOBAL_QUOTE"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"Information\":\"Thank you for using Alpha Vantage! " +
                                "Our standard API rate limit is 25 requests per day.\"}")));

        assertThatThrownBy(() -> provider.getQuote("AAPL"))
                .isInstanceOf(ExternalApiException.class);
    }

    @Test
    @DisplayName("deve lançar StockNotFoundException quando Error Message contém 'invalid api call'")
    void deveLancarStockNotFoundParaInvalidApiCall() {
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("GLOBAL_QUOTE"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"Error Message\":\"Invalid API call. Please retry or visit...\"}")));

        assertThatThrownBy(() -> provider.getQuote("AAPL"))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    @DisplayName("deve retornar dados da empresa corretamente")
    void deveRetornarCompanyInfo() {
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("OVERVIEW"))
                .withQueryParam("symbol", equalTo("AAPL"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"Symbol\":\"AAPL\",\"Name\":\"Apple Inc\",\"Exchange\":\"NASDAQ\"," +
                                "\"Country\":\"USA\",\"Sector\":\"TECHNOLOGY\",\"Industry\":\"ELECTRONIC COMPUTERS\"," +
                                "\"Currency\":\"USD\",\"OfficialSite\":\"https://www.apple.com\"}")));

        CompanyInfoResponse result = provider.getCompanyInfo("AAPL");

        assertThat(result.symbol()).isEqualTo("AAPL");
        assertThat(result.name()).isEqualTo("Apple Inc");
        assertThat(result.sector()).isEqualTo("TECHNOLOGY");
        assertThat(result.provider()).isEqualTo("alpha-vantage");
    }

    @Test
    @DisplayName("deve lançar StockNotFoundException quando empresa não existe")
    void deveLancarStockNotFoundParaEmpresaInexistente() {
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("OVERVIEW"))
                .withQueryParam("symbol", equalTo("UNKNOWN"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        assertThatThrownBy(() -> provider.getCompanyInfo("UNKNOWN"))
                .isInstanceOf(StockNotFoundException.class);
    }
}
