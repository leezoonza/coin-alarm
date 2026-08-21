package com.zoonza.coinalarm.market.internal.adapter.out.binance.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BinanceExchangeInfoResponse(
        List<SymbolResponse> symbols
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SymbolResponse(
            String symbol,
            String contractType,
            String status,
            String baseAsset,
            String quoteAsset,
            List<FilterResponse> filters
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FilterResponse(
            String filterType,
            String tickSize
    ) {
    }
}
