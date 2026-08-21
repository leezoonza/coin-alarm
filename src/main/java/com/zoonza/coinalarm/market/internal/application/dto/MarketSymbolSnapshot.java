package com.zoonza.coinalarm.market.internal.application.dto;

import com.zoonza.coinalarm.market.internal.domain.MarketType;

import java.math.BigDecimal;

public record MarketSymbolSnapshot(
        String code,
        MarketType marketType,
        String baseAsset,
        String quoteAsset,
        BigDecimal tickSize,
        String status
) {
}
