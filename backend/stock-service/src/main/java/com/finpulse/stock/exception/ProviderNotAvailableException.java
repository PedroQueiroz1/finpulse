package com.finpulse.stock.exception;

public class ProviderNotAvailableException extends RuntimeException {
    public ProviderNotAvailableException(String provider) {
        super("Provedor não disponível: " + provider);
    }
}