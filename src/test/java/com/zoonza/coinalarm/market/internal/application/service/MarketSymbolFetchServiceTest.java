package com.zoonza.coinalarm.market.internal.application.service;

import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSnapshot;
import com.zoonza.coinalarm.market.internal.application.port.out.MarketSymbolSourcePort;
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
class MarketSymbolFetchServiceTest {
    @Mock
    private MarketSymbolSourcePort marketSymbolSourcePort;

    @Test
    @DisplayName("외부 소스에서 모든 마켓 심볼을 조회한다")
    void fetchesAllMarketSymbolsFromSource() {
        MarketSymbolSnapshot snapshot = snapshot(
                "BTCUSDT",
                MarketType.USD_M_PERPETUAL_FUTURES,
                "BTC",
                "USDT",
                new BigDecimal("0.10"),
                "TRADING"
        );
        when(marketSymbolSourcePort.fetchAll()).thenReturn(List.of(snapshot));

        MarketSymbolFetchService service = new MarketSymbolFetchService(marketSymbolSourcePort);

        List<MarketSymbolSnapshot> result = service.fetchAll();

        assertThat(result).containsExactly(snapshot);
    }
}
