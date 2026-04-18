package com.finpulse.stock.dto;

public record CompanyInfoResponse(
        String symbol,
        String name,
        String description,
        String exchange,
        String country,
        String sector,
        String industry,
        String currency,
        String website,
        String logoUrl,
        String provider
) {}