package com.zoonza.coinalarm.market.internal.domain;

import java.util.List;

public interface MarketSymbolRepository {
    List<MarketSymbol> findAll();

    void saveAll(List<MarketSymbol> marketSymbols);
}
