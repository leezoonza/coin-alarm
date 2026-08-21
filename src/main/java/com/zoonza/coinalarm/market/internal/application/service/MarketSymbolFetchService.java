package com.zoonza.coinalarm.market.internal.application.service;

import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSnapshot;
import com.zoonza.coinalarm.market.internal.application.port.out.MarketSymbolSourcePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketSymbolFetchService {
    private final MarketSymbolSourcePort marketSymbolSourcePort;

    public List<MarketSymbolSnapshot> fetchAll() {
        return marketSymbolSourcePort.fetchAll();
    }
}
