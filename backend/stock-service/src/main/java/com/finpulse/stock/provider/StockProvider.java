package com.finpulse.stock.provider;

import com.finpulse.stock.dto.CompanyInfoResponse;
import com.finpulse.stock.dto.StockQuoteResponse;

/**
 * Strategy Pattern — contrato comum para todos os provedores de cotação.
 * Cada provedor externo (Alpha Vantage, Finnhub, etc.) implementa essa interface.
 * Isso desacopla o service das APIs externas e permite trocar/adicionar provedores
 * sem alterar o código cliente. É o princípio O do SOLID (Open/Closed) em ação.
 */
public interface StockProvider {

    /** Identificador único do provedor (ex: "alpha-vantage", "finnhub"). */
    String getName();

    /** Busca a cotação atual de um ticker. */
    StockQuoteResponse getQuote(String symbol);

    /** Busca dados cadastrais da empresa. */
    CompanyInfoResponse getCompanyInfo(String symbol);
}