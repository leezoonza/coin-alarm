package com.zoonza.coinalarm.market.internal.adapter.out.binance;

import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSnapshot;
import com.zoonza.coinalarm.market.internal.domain.MarketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BinanceMarketSymbolSourceAdapterTest {
    private static final String EXCHANGE_INFO_URL = "https://fapi.binance.com/fapi/v1/exchangeInfo";

    private MockRestServiceServer server;
    private BinanceMarketSymbolSourceAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://fapi.binance.com");
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new BinanceMarketSymbolSourceAdapter(builder.build());
    }

    @Test
    @DisplayName("지원하는 USDT 무기한 계약만 마켓 심볼로 변환한다")
    void fetchesSupportedUsdtPerpetualSymbols() {
        String response = """
                {
                  "symbols": [
                    {
                      "symbol": "BTCUSDT",
                      "contractType": "PERPETUAL",
                      "status": "TRADING",
                      "baseAsset": "BTC",
                      "quoteAsset": "USDT",
                      "filters": [
                        {"filterType": "LOT_SIZE", "tickSize": null},
                        {"filterType": "PRICE_FILTER", "tickSize": "0.10"}
                      ]
                    },
                    {
                      "symbol": "XAUUSDT",
                      "contractType": "TRADIFI_PERPETUAL",
                      "status": "TRADING",
                      "baseAsset": "XAU",
                      "quoteAsset": "USDT",
                      "filters": [
                        {"filterType": "PRICE_FILTER", "tickSize": "0.01"}
                      ]
                    },
                    {
                      "symbol": "BTCUSDC",
                      "contractType": "PERPETUAL",
                      "status": "TRADING",
                      "baseAsset": "BTC",
                      "quoteAsset": "USDC",
                      "filters": [
                        {"filterType": "PRICE_FILTER", "tickSize": "0.10"}
                      ]
                    },
                    {
                      "symbol": "ETHUSDT_260925",
                      "contractType": "CURRENT_QUARTER",
                      "status": "TRADING",
                      "baseAsset": "ETH",
                      "quoteAsset": "USDT",
                      "filters": [
                        {"filterType": "PRICE_FILTER", "tickSize": "0.01"}
                      ]
                    }
                  ]
                }
                """;
        server.expect(once(), requestTo(EXCHANGE_INFO_URL))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        List<MarketSymbolSnapshot> result = adapter.fetchAll();

        assertThat(result).containsExactly(
                new MarketSymbolSnapshot(
                        "BTCUSDT",
                        MarketType.USD_M_PERPETUAL_FUTURES,
                        "BTC",
                        "USDT",
                        new java.math.BigDecimal("0.10"),
                        "TRADING"
                ),
                new MarketSymbolSnapshot(
                        "XAUUSDT",
                        MarketType.USD_M_PERPETUAL_FUTURES,
                        "XAU",
                        "USDT",
                        new java.math.BigDecimal("0.01"),
                        "TRADING"
                )
        );
        server.verify();
    }

    @Test
    @DisplayName("지원하는 종목에 가격 필터가 없으면 실패한다")
    void rejectsSupportedSymbolWithoutPriceFilter() {
        String response = """
                {
                  "symbols": [
                    {
                      "symbol": "BTCUSDT",
                      "contractType": "PERPETUAL",
                      "status": "TRADING",
                      "baseAsset": "BTC",
                      "quoteAsset": "USDT",
                      "filters": [
                        {"filterType": "LOT_SIZE", "tickSize": null}
                      ]
                    }
                  ]
                }
                """;
        server.expect(once(), requestTo(EXCHANGE_INFO_URL))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        assertThatThrownBy(adapter::fetchAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Binance 종목에 PRICE_FILTER가 없습니다: BTCUSDT");

        server.verify();
    }
}
