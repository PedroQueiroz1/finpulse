package com.finpulse.stock.service;

import com.finpulse.stock.dto.CompanyInfoResponse;
import com.finpulse.stock.dto.StockQuoteResponse;
import com.finpulse.stock.provider.StockProviderFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final StockProviderFacade providerFacade;

    public StockService(StockProviderFacade providerFacade) {
        this.providerFacade = providerFacade;
    }

    /**
     * Cotação com cache de 60 segundos.
     * A chave do cache é "quotes::{symbol}" no Redis.
     */
    @Cacheable(value = "quotes", key = "#symbol.toUpperCase()")
    public StockQuoteResponse getQuote(String symbol) {
        log.info("Cache MISS — buscando cotação de {} via provider", symbol);
        return providerFacade.getQuote(symbol.toUpperCase());
    }

    @Cacheable(value = "quotes",
               key = "#symbol.toUpperCase() + '::' + #provider")
    public StockQuoteResponse getQuoteFromProvider(String symbol, String provider) {
        log.info("Cache MISS — buscando {} via {}", symbol, provider);
        return providerFacade.getQuote(symbol.toUpperCase(), provider);
    }

    /**
     * Dados da empresa têm cache de 24 horas (configurado no Redis por chave).
     */
    @Cacheable(value = "companies", key = "#symbol.toUpperCase()")
    public CompanyInfoResponse getCompanyInfo(String symbol) {
        log.info("Cache MISS — buscando empresa {} via provider", symbol);
        return providerFacade.getCompanyInfo(symbol.toUpperCase());
    }

    /**
     * Força refresh do cache de um ticker.
     */
    @CacheEvict(value = "quotes", key = "#symbol.toUpperCase()")
    public void evictQuoteCache(String symbol) {
        log.info("Cache evictado para {}", symbol);
    }

    public List<String> getAvailableProviders() {
        return providerFacade.getAvailableProviders();
    }
}