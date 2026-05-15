package com.finpulse.stock.helper;

import com.github.tomakehurst.wiremock.WireMockServer;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class StockApiStubs {

    private final WireMockServer wireMock;

    public StockApiStubs(WireMockServer wireMock) {
        this.wireMock = wireMock;
    }

    public void stubAlphaVantageQuoteSuccess(String symbol) {
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("GLOBAL_QUOTE"))
                .withQueryParam("symbol", equalTo(symbol))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFixture("alpha-vantage-aapl-quote-success.json"))));
    }

    public void stubAlphaVantageCompanySuccess(String symbol) {
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("OVERVIEW"))
                .withQueryParam("symbol", equalTo(symbol))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFixture("alpha-vantage-aapl-company-success.json"))));
    }

    public void stubAlphaVantageRateLimited() {
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("GLOBAL_QUOTE"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFixture("alpha-vantage-rate-limited.json"))));
    }

    public void stubAlphaVantageEmptyQuote(String symbol) {
        wireMock.stubFor(get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("GLOBAL_QUOTE"))
                .withQueryParam("symbol", equalTo(symbol))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"Global Quote\":{}}")));
    }

    public void stubFinnhubQuoteSuccess(String symbol) {
        wireMock.stubFor(get(urlPathEqualTo("/quote"))
                .withQueryParam("symbol", equalTo(symbol))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFixture("finnhub-aapl-quote-success.json"))));
    }

    public void stubFinnhubQuoteNotFound(String symbol) {
        wireMock.stubFor(get(urlPathEqualTo("/quote"))
                .withQueryParam("symbol", equalTo(symbol))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"c\":0,\"d\":0,\"dp\":0,\"h\":0,\"l\":0,\"o\":0,\"pc\":0,\"t\":0}")));
    }

    public void reset() {
        wireMock.resetAll();
    }

    private String readFixture(String filename) {
        try {
            URL url = getClass().getClassLoader().getResource("wiremock/" + filename);
            if (url == null) throw new IllegalStateException("Fixture not found: " + filename);
            return new String(url.openStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read fixture: " + filename, e);
        }
    }
}
