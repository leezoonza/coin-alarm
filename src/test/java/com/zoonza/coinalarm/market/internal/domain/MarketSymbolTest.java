package com.zoonza.coinalarm.market.internal.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MarketSymbolTest {
    private static final Instant REGISTERED_AT = Instant.parse("2026-08-20T00:00:00Z");

    @Test
    @DisplayName("마켓 심볼을 등록하면 등록 시각과 수정 시각이 동일하게 설정된다")
    void registersMarketSymbol() {
        MarketSymbol marketSymbol = MarketSymbol.register(
                "BTCUSDT",
                MarketType.USD_M_PERPETUAL_FUTURES,
                "BTC",
                "USDT",
                new BigDecimal("0.10"),
                "TRADING",
                REGISTERED_AT
        );

        assertThat(marketSymbol.getCode()).isEqualTo("BTCUSDT");
        assertThat(marketSymbol.getMarketType()).isEqualTo(MarketType.USD_M_PERPETUAL_FUTURES);
        assertThat(marketSymbol.getBaseAsset()).isEqualTo("BTC");
        assertThat(marketSymbol.getQuoteAsset()).isEqualTo("USDT");
        assertThat(marketSymbol.getTickSize()).isEqualByComparingTo("0.10");
        assertThat(marketSymbol.getStatus()).isEqualTo("TRADING");
        assertThat(marketSymbol.getRegisteredAt()).isEqualTo(REGISTERED_AT);
        assertThat(marketSymbol.getUpdatedAt()).isEqualTo(REGISTERED_AT);
    }

    @Test
    @DisplayName("마켓 정보가 변경되면 값을 갱신하고 수정 시각을 기록한다")
    void synchronizesChangedMarketInformation() {
        MarketSymbol marketSymbol = MarketSymbol.register(
                "BTCUSDT",
                MarketType.USD_M_PERPETUAL_FUTURES,
                "BTC",
                "USDT",
                new BigDecimal("0.10"),
                "PENDING_TRADING",
                REGISTERED_AT
        );
        Instant updatedAt = REGISTERED_AT.plusSeconds(60);

        boolean changed = marketSymbol.synchronize(
                "XBT",
                "USDC",
                new BigDecimal("0.01"),
                "TRADING",
                updatedAt
        );

        assertThat(changed).isTrue();
        assertThat(marketSymbol.getBaseAsset()).isEqualTo("XBT");
        assertThat(marketSymbol.getQuoteAsset()).isEqualTo("USDC");
        assertThat(marketSymbol.getTickSize()).isEqualByComparingTo("0.01");
        assertThat(marketSymbol.getStatus()).isEqualTo("TRADING");
        assertThat(marketSymbol.getRegisteredAt()).isEqualTo(REGISTERED_AT);
        assertThat(marketSymbol.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("값이 동일하면 소수점 자릿수가 달라도 변경하지 않는다")
    void doesNotSynchronizeEquivalentMarketInformation() {
        MarketSymbol marketSymbol = MarketSymbol.register(
                "BTCUSDT",
                MarketType.USD_M_PERPETUAL_FUTURES,
                "BTC",
                "USDT",
                new BigDecimal("0.10"),
                "TRADING",
                REGISTERED_AT
        );

        boolean changed = marketSymbol.synchronize(
                "BTC",
                "USDT",
                new BigDecimal("0.100"),
                "TRADING",
                REGISTERED_AT.plusSeconds(60)
        );

        assertThat(changed).isFalse();
        assertThat(marketSymbol.getUpdatedAt()).isEqualTo(REGISTERED_AT);
    }
}
