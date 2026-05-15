package com.finpulse.stock.provider;

import com.finpulse.stock.dto.CompanyInfoResponse;
import com.finpulse.stock.dto.StockQuoteResponse;
import com.finpulse.stock.exception.StockNotFoundException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FinnhubProvider - Testes Unitários")
class FinnhubProviderTest {

    static WireMockServer wireMock;
    static FinnhubProvider provider;

    @BeforeAll
    static void setUpClass() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        provider = new FinnhubProvider(
                "http://localhost:" + wireMock.port(), "test-token");
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
    @DisplayName("deve retornar cotação com preço correto")
    void deveRetornarCotacaoCorreta() {
        wireMock.stubFor(get(urlPathEqualTo("/quote"))
                .withQueryParam("symbol", equalTo("AAPL"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"c\":177.30,\"d\":1.30,\"dp\":0.74,\"h\":178.90," +
                                "\"l\":174.20,\"o\":175.50,\"pc\":176.00,\"t\":1705330800}")));

        StockQuoteResponse result = provider.getQuote("AAPL");

        assertThat(result.symbol()).isEqualTo("AAPL");
        assertThat(result.price()).isEqualByComparingTo(new BigDecimal("177.30"));
        assertThat(result.provider()).isEqualTo("finnhub");
        assertThat(result.dayHigh()).isEqualByComparingTo(new BigDecimal("178.90"));
    }

    @Test
    @DisplayName("deve lançar StockNotFoundException quando preço atual é zero")
    void deveLancarStockNotFoundParaPrecoZero() {
        wireMock.stubFor(get(urlPathEqualTo("/quote"))
                .withQueryParam("symbol", equalTo("INVALID"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"c\":0,\"d\":0,\"dp\":0,\"h\":0,\"l\":0,\"o\":0,\"pc\":0,\"t\":0}")));

        assertThatThrownBy(() -> provider.getQuote("INVALID"))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    @DisplayName("deve retornar dados da empresa corretamente")
    void deveRetornarCompanyInfo() {
        wireMock.stubFor(get(urlPathEqualTo("/stock/profile2"))
                .withQueryParam("symbol", equalTo("AAPL"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"ticker\":\"AAPL\",\"name\":\"Apple Inc\",\"exchange\":\"NASDAQ\"," +
                                "\"country\":\"US\",\"currency\":\"USD\",\"finnhubIndustry\":\"Technology\"," +
                                "\"weburl\":\"https://www.apple.com\",\"logo\":\"https://logo.url/aapl.png\"}")));

        CompanyInfoResponse result = provider.getCompanyInfo("AAPL");

        assertThat(result.symbol()).isEqualTo("AAPL");
        assertThat(result.name()).isEqualTo("Apple Inc");
        assertThat(result.provider()).isEqualTo("finnhub");
    }

    @Test
    @DisplayName("deve lançar StockNotFoundException quando empresa não existe no Finnhub")
    void deveLancarStockNotFoundParaEmpresaVazia() {
        wireMock.stubFor(get(urlPathEqualTo("/stock/profile2"))
                .withQueryParam("symbol", equalTo("UNKNOWN"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        assertThatThrownBy(() -> provider.getCompanyInfo("UNKNOWN"))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    @DisplayName("getName deve retornar finnhub")
    void deveRetornarNomeCorreto() {
        assertThat(provider.getName()).isEqualTo("finnhub");
    }
}
