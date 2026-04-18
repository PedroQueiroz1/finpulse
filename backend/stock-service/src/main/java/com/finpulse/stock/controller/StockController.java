package com.finpulse.stock.controller;
import com.finpulse.stock.dto.CompanyInfoResponse;
import com.finpulse.stock.dto.StockQuoteResponse;
import com.finpulse.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@Tag(name = "Cotações", description = "Consulta de cotações e dados de empresas")
public class StockController {

    private static final Logger log = LoggerFactory.getLogger(StockController.class);

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/{symbol}/quote")
    @Operation(summary = "Cotação de uma ação",
               description = "Busca a cotação atual. Usa cache Redis (TTL 60s).")
    public ResponseEntity<StockQuoteResponse> getQuote(@PathVariable String symbol) {
        log.info("GET /api/stocks/{}/quote", symbol);
        return ResponseEntity.ok(stockService.getQuote(symbol));
    }

    @GetMapping("/{symbol}/quote/{provider}")
    @Operation(summary = "Cotação usando provider específico",
               description = "Permite escolher entre alpha-vantage ou finnhub")
    public ResponseEntity<StockQuoteResponse> getQuoteFromProvider(
            @PathVariable String symbol,
            @PathVariable String provider) {
        return ResponseEntity.ok(stockService.getQuoteFromProvider(symbol, provider));
    }

    @GetMapping("/{symbol}/company")
    @Operation(summary = "Dados cadastrais da empresa",
               description = "Nome, setor, país, etc. Cache de 24h.")
    public ResponseEntity<CompanyInfoResponse> getCompany(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getCompanyInfo(symbol));
    }

    @DeleteMapping("/{symbol}/cache")
    @Operation(summary = "Limpar cache de um ticker",
               description = "Força a próxima requisição a buscar do provider")
    public ResponseEntity<Void> evictCache(@PathVariable String symbol) {
        stockService.evictQuoteCache(symbol);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/providers")
    @Operation(summary = "Listar providers disponíveis")
    public ResponseEntity<List<String>> getProviders() {
        return ResponseEntity.ok(stockService.getAvailableProviders());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Stock Service is running!");
    }
}