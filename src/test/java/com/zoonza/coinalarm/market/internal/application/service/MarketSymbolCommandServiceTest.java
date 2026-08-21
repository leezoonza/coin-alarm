package com.zoonza.coinalarm.market.internal.application.service;

import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSnapshot;
import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSyncResult;
import com.zoonza.coinalarm.market.internal.domain.MarketSymbol;
import com.zoonza.coinalarm.market.internal.domain.MarketSymbolRepository;
import com.zoonza.coinalarm.market.internal.domain.MarketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static com.zoonza.coinalarm.market.fixture.MarketSymbolFixture.marketSymbol;
import static com.zoonza.coinalarm.market.fixture.MarketSymbolFixture.snapshot;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketSymbolCommandServiceTest {
    private static final Instant REGISTERED_AT = Instant.parse("2026-08-20T00:00:00Z");
    private static final Instant SYNCHRONIZED_AT = Instant.parse("2026-08-21T00:00:00Z");

    @Mock
    private MarketSymbolRepository marketSymbolRepository;

    private MarketSymbolCommandService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(SYNCHRONIZED_AT, ZoneOffset.UTC);

        service = new MarketSymbolCommandService(clock, marketSymbolRepository);
    }

    @Test
    @DisplayName("새로운 마켓 심볼을 등록한다")
    void registersNewMarketSymbols() {
        when(marketSymbolRepository.findAll()).thenReturn(List.of());

        List<MarketSymbolSnapshot> snapshots = List.of(
                snapshot(
                        "BTCUSDT",
                        MarketType.USD_M_PERPETUAL_FUTURES,
                        "BTC",
                        "USDT",
                        new BigDecimal("0.10"),
                        "TRADING"
                ),
                snapshot(
                        "ETHUSDT",
                        MarketType.USD_M_PERPETUAL_FUTURES,
                        "ETH",
                        "USDT",
                        new BigDecimal("0.01"),
                        "TRADING"
                )
        );

        MarketSymbolSyncResult result = service.synchronize(snapshots);

        assertThat(result).isEqualTo(new MarketSymbolSyncResult(2, 0, 0));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MarketSymbol>> symbolsCaptor = ArgumentCaptor.forClass(List.class);

        verify(marketSymbolRepository).saveAll(symbolsCaptor.capture());

        assertThat(symbolsCaptor.getValue())
                .extracting(MarketSymbol::getCode)
                .containsExactly("BTCUSDT", "ETHUSDT");

        assertThat(symbolsCaptor.getValue())
                .allSatisfy(symbol -> {
                    assertThat(symbol.getRegisteredAt()).isEqualTo(SYNCHRONIZED_AT);
                    assertThat(symbol.getUpdatedAt()).isEqualTo(SYNCHRONIZED_AT);
                });
    }

    @Test
    @DisplayName("기존 마켓 심볼 중 변경된 심볼만 갱신해 저장한다")
    void updatesOnlyChangedMarketSymbols() {
        MarketSymbol changedSymbol = marketSymbol(
                "BTCUSDT",
                MarketType.USD_M_PERPETUAL_FUTURES,
                "BTC",
                "USDT",
                new BigDecimal("0.10"),
                "PENDING_TRADING",
                REGISTERED_AT
        );
        MarketSymbol unchangedSymbol = marketSymbol(
                "ETHUSDT",
                MarketType.USD_M_PERPETUAL_FUTURES,
                "ETH",
                "USDT",
                new BigDecimal("0.01"),
                "TRADING",
                REGISTERED_AT
        );
        when(marketSymbolRepository.findAll()).thenReturn(List.of(changedSymbol, unchangedSymbol));

        List<MarketSymbolSnapshot> snapshots = List.of(
                snapshot(
                        "BTCUSDT",
                        MarketType.USD_M_PERPETUAL_FUTURES,
                        "BTC",
                        "USDT",
                        new BigDecimal("0.10"),
                        "TRADING"
                ),
                snapshot(
                        "ETHUSDT",
                        MarketType.USD_M_PERPETUAL_FUTURES,
                        "ETH",
                        "USDT",
                        new BigDecimal("0.010"),
                        "TRADING"
                )
        );

        MarketSymbolSyncResult result = service.synchronize(snapshots);

        assertThat(result).isEqualTo(new MarketSymbolSyncResult(0, 1, 1));
        assertThat(changedSymbol.getStatus()).isEqualTo("TRADING");
        assertThat(changedSymbol.getUpdatedAt()).isEqualTo(SYNCHRONIZED_AT);
        assertThat(unchangedSymbol.getUpdatedAt()).isEqualTo(REGISTERED_AT);

        verify(marketSymbolRepository).saveAll(List.of(changedSymbol));
    }

    @Test
    @DisplayName("변경된 마켓 심볼이 없으면 저장하지 않는다")
    void doesNotSaveWhenNothingChanged() {
        MarketSymbol existingSymbol = marketSymbol(
                "BTCUSDT",
                MarketType.USD_M_PERPETUAL_FUTURES,
                "BTC",
                "USDT",
                new BigDecimal("0.10"),
                "TRADING",
                REGISTERED_AT
        );

        when(marketSymbolRepository.findAll()).thenReturn(List.of(existingSymbol));

        List<MarketSymbolSnapshot> snapshots = List.of(snapshot(
                "BTCUSDT",
                MarketType.USD_M_PERPETUAL_FUTURES,
                "BTC",
                "USDT",
                new BigDecimal("0.100"),
                "TRADING"
        ));

        MarketSymbolSyncResult result = service.synchronize(snapshots);

        assertThat(result).isEqualTo(new MarketSymbolSyncResult(0, 0, 1));

        verify(marketSymbolRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    @DisplayName("조회된 마켓 심볼이 없으면 빈 동기화 결과를 반환한다")
    void returnsEmptyResultForEmptySnapshots() {
        when(marketSymbolRepository.findAll()).thenReturn(List.of());

        MarketSymbolSyncResult result = service.synchronize(List.of());

        assertThat(result).isEqualTo(new MarketSymbolSyncResult(0, 0, 0));

        verify(marketSymbolRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }
}
