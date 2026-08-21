package com.zoonza.coinalarm.market.fixture;

import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSnapshot;
import com.zoonza.coinalarm.market.internal.domain.MarketSymbol;
import com.zoonza.coinalarm.market.internal.domain.MarketType;

import java.math.BigDecimal;
import java.time.Instant;

public final class MarketSymbolFixture {
    private MarketSymbolFixture() {
    }

    public static MarketSymbol marketSymbol(
            String code,
            MarketType marketType,
            String baseAsset,
            String quoteAsset,
            BigDecimal tickSize,
            String status,
            Instant registeredAt
    ) {
        return MarketSymbol.register(
                code,
                marketType,
                baseAsset,
                quoteAsset,
                tickSize,
                status,
                registeredAt
        );
    }

    public static MarketSymbolSnapshot snapshot(
            String code,
            MarketType marketType,
            String baseAsset,
            String quoteAsset,
            BigDecimal tickSize,
            String status
    ) {
        return new MarketSymbolSnapshot(
                code,
                marketType,
                baseAsset,
                quoteAsset,
                tickSize,
                status
        );
    }
}
