package com.finpulse.stock.exception;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(String symbol) {
        super("Cotação não encontrada para o ticker: " + symbol);
    }
}