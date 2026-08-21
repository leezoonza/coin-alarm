package com.zoonza.coinalarm.market.internal.adapter.out.persistence;

import com.zoonza.coinalarm.market.internal.domain.MarketSymbol;
import com.zoonza.coinalarm.market.internal.domain.MarketSymbolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaMarketSymbolRepositoryAdapter implements MarketSymbolRepository {
    private final MarketSymbolJpaRepository repository;

    @Override
    public List<MarketSymbol> findAll() {
        return repository.findAll();
    }

    @Override
    public void saveAll(List<MarketSymbol> marketSymbols) {
        repository.saveAll(marketSymbols);
    }
}
