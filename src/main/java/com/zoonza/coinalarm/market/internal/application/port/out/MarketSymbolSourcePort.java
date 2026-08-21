package com.zoonza.coinalarm.market.internal.application.port.out;

import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSnapshot;

import java.util.List;

public interface MarketSymbolSourcePort {
    List<MarketSymbolSnapshot> fetchAll();
}
