package com.zoonza.coinalarm.market.internal.adapter.out.binance;

import com.zoonza.coinalarm.market.internal.adapter.out.binance.dto.BinanceExchangeInfoResponse;
import com.zoonza.coinalarm.market.internal.adapter.out.binance.dto.BinanceExchangeInfoResponse.FilterResponse;
import com.zoonza.coinalarm.market.internal.adapter.out.binance.dto.BinanceExchangeInfoResponse.SymbolResponse;
import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSnapshot;
import com.zoonza.coinalarm.market.internal.application.port.out.MarketSymbolSourcePort;
import com.zoonza.coinalarm.market.internal.domain.MarketType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BinanceMarketSymbolSourceAdapter implements MarketSymbolSourcePort {
    private static final String EXCHANGE_INFO_URI = "/fapi/v1/exchangeInfo";
    private static final String SUPPORTED_QUOTE_ASSET = "USDT";
    private static final String PRICE_FILTER = "PRICE_FILTER";
    private static final Set<String> SUPPORTED_CONTRACT_TYPES = Set.of("PERPETUAL", "TRADIFI_PERPETUAL");

    private final RestClient binanceRestClient;

    @Override
    public List<MarketSymbolSnapshot> fetchAll() {
        BinanceExchangeInfoResponse response = binanceRestClient.get()
                .uri(EXCHANGE_INFO_URI)
                .retrieve()
                .body(BinanceExchangeInfoResponse.class);

        return response.symbols().stream()
                .filter(this::isSupported)
                .map(this::toSnapshot)
                .toList();
    }

    private boolean isSupported(SymbolResponse symbol) {
        return SUPPORTED_CONTRACT_TYPES.contains(symbol.contractType())
                && SUPPORTED_QUOTE_ASSET.equals(symbol.quoteAsset());
    }

    private MarketSymbolSnapshot toSnapshot(SymbolResponse symbol) {
        return new MarketSymbolSnapshot(
                symbol.symbol(),
                MarketType.USD_M_PERPETUAL_FUTURES,
                symbol.baseAsset(),
                symbol.quoteAsset(),
                extractTickSize(symbol),
                symbol.status()
        );
    }

    private BigDecimal extractTickSize(SymbolResponse symbol) {
        String tickSize = symbol.filters().stream()
                .filter(filter -> PRICE_FILTER.equals(filter.filterType()))
                .map(FilterResponse::tickSize)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Binance 종목에 PRICE_FILTER가 없습니다: " + symbol.symbol()));

        return new BigDecimal(tickSize);
    }
}
