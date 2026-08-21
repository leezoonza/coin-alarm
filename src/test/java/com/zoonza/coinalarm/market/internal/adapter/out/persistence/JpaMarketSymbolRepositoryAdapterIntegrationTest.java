package com.zoonza.coinalarm.market.internal.adapter.out.persistence;

import com.zoonza.coinalarm.TestcontainersConfiguration;
import com.zoonza.coinalarm.market.internal.domain.MarketSymbol;
import com.zoonza.coinalarm.market.internal.domain.MarketSymbolRepository;
import com.zoonza.coinalarm.market.internal.domain.MarketType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.zoonza.coinalarm.market.fixture.MarketSymbolFixture.marketSymbol;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({
        TestcontainersConfiguration.class,
        JpaMarketSymbolRepositoryAdapter.class
})
class JpaMarketSymbolRepositoryAdapterIntegrationTest {
    private static final Instant REGISTERED_AT = Instant.parse("2026-08-21T00:00:00Z");

    @Autowired
    private MarketSymbolRepository marketSymbolRepository;

    @Test
    @DisplayName("마켓 심볼을 저장하고 전체 조회한다")
    void savesAndFindsAllMarketSymbols() {
        MarketSymbol btcSymbol = marketSymbol(
                "BTCUSDT",
                MarketType.USD_M_PERPETUAL_FUTURES,
                "BTC",
                "USDT",
                new BigDecimal("0.10"),
                "TRADING",
                REGISTERED_AT
        );

        MarketSymbol ethSymbol = marketSymbol(
                "ETHUSDT",
                MarketType.USD_M_PERPETUAL_FUTURES,
                "ETH",
                "USDT",
                new BigDecimal("0.01"),
                "TRADING",
                REGISTERED_AT
        );

        marketSymbolRepository.saveAll(List.of(btcSymbol, ethSymbol));

        assertThat(btcSymbol.getId()).isNotNull();
        assertThat(ethSymbol.getId()).isNotNull();
        assertThat(marketSymbolRepository.findAll())
                .extracting(MarketSymbol::getCode)
                .containsExactlyInAnyOrder("BTCUSDT", "ETHUSDT");
    }
}
