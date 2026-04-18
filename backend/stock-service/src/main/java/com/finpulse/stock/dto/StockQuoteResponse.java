package com.finpulse.stock.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
Valores monetários sempre BigDecimal. Maior precisão... DINHEIRO = BIGDECIMAL
*/
public record StockQuoteResponse(
        String symbol,
        BigDecimal price,
        BigDecimal change,
        BigDecimal changePercent,
        BigDecimal dayHigh,
        BigDecimal dayLow,
        BigDecimal dayOpen,
        BigDecimal previousClose,
        Long volume,
        String provider,
        LocalDateTime fetchedAt
) {}