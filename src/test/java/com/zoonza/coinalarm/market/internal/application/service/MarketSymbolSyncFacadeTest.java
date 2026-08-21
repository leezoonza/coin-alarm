package com.zoonza.coinalarm.market.internal.application.service;

import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSnapshot;
import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSyncResult;
import com.zoonza.coinalarm.market.internal.domain.MarketType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static com.zoonza.coinalarm.market.fixture.MarketSymbolFixture.snapshot;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketSymbolSyncFacadeTest {
    @Mock
    private MarketSymbolFetchService marketSymbolFetchService;

    @Mock
    private MarketSymbolCommandService marketSymbolCommandService;

    @Test
    @DisplayName("조회한 마켓 심볼을 동기화하고 결과를 반환한다")
    void fetchesAndSynchronizesMarketSymbols() {
        List<MarketSymbolSnapshot> snapshots = List.of(snapshot(
                "BTCUSDT",
                MarketType.USD_M_PERPETUAL_FUTURES,
                "BTC",
                "USDT",
                new BigDecimal("0.10"),
                "TRADING"
        ));

        MarketSymbolSyncResult syncResult = new MarketSymbolSyncResult(1, 0, 0);

        when(marketSymbolFetchService.fetchAll()).thenReturn(snapshots);
        when(marketSymbolCommandService.synchronize(snapshots)).thenReturn(syncResult);

        MarketSymbolSyncFacade facade = new MarketSymbolSyncFacade(
                marketSymbolFetchService,
                marketSymbolCommandService
        );

        MarketSymbolSyncResult result = facade.syncMarketSymbols();

        assertThat(result).isSameAs(syncResult);
    }
}
