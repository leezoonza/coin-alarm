package com.zoonza.coinalarm.market.internal.application.service;

import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSnapshot;
import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSyncResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MarketSymbolSyncFacade {
    private final MarketSymbolFetchService marketSymbolFetchService;
    private final MarketSymbolCommandService marketSymbolCommandService;

    public MarketSymbolSyncResult syncMarketSymbols() {
        List<MarketSymbolSnapshot> snapshots = marketSymbolFetchService.fetchAll();

        return marketSymbolCommandService.synchronize(snapshots);
    }
}
