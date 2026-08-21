package com.zoonza.coinalarm.market.internal.adapter.out.persistence;

import com.zoonza.coinalarm.market.internal.domain.MarketSymbol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarketSymbolJpaRepository extends JpaRepository<MarketSymbol, Long> {
    Optional<MarketSymbol> findByCode(String code);
}
