package com.zoonza.coinalarm.market.internal.application.service;

import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSnapshot;
import com.zoonza.coinalarm.market.internal.application.dto.MarketSymbolSyncResult;
import com.zoonza.coinalarm.market.internal.domain.MarketSymbol;
import com.zoonza.coinalarm.market.internal.domain.MarketSymbolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketSymbolCommandService {
    private final Clock clock;
    private final MarketSymbolRepository marketSymbolRepository;

    @Transactional
    public MarketSymbolSyncResult synchronize(List<MarketSymbolSnapshot> snapshots) {
        Map<String, MarketSymbol> existingSymbols = findExistingSymbols();

        Instant synchronizedAt = clock.instant();
        List<MarketSymbol> changedSymbols = new ArrayList<>();

        int registeredCount = 0;
        int updatedCount = 0;
        int unchangedCount = 0;

        for (MarketSymbolSnapshot snapshot : snapshots) {
            MarketSymbol existing = existingSymbols.get(snapshot.code());

            if (existing == null) {
                MarketSymbol marketSymbol = createMarketSymbol(snapshot, synchronizedAt);
                changedSymbols.add(marketSymbol);
                registeredCount++;

                continue;
            }

            boolean updated = synchronizeMarketSymbol(existing, snapshot, synchronizedAt);

            if (updated) {
                changedSymbols.add(existing);
                updatedCount++;
            } else {
                unchangedCount++;
            }
        }

        saveChangedSymbols(changedSymbols);

        return new MarketSymbolSyncResult(
                registeredCount,
                updatedCount,
                unchangedCount
        );
    }

    private Map<String, MarketSymbol> findExistingSymbols() {
        return marketSymbolRepository.findAll()
                .stream()
                .collect(Collectors.toMap(MarketSymbol::getCode, Function.identity()));
    }

    private MarketSymbol createMarketSymbol(MarketSymbolSnapshot snapshot, Instant synchronizedAt) {
        return MarketSymbol.register(
                snapshot.code(),
                snapshot.marketType(),
                snapshot.baseAsset(),
                snapshot.quoteAsset(),
                snapshot.tickSize(),
                snapshot.status(),
                synchronizedAt
        );
    }

    private boolean synchronizeMarketSymbol(
            MarketSymbol marketSymbol,
            MarketSymbolSnapshot snapshot,
            Instant synchronizedAt
    ) {
        return marketSymbol.synchronize(
                snapshot.baseAsset(),
                snapshot.quoteAsset(),
                snapshot.tickSize(),
                snapshot.status(),
                synchronizedAt
        );
    }

    private void saveChangedSymbols(List<MarketSymbol> changedSymbols) {
        if (!changedSymbols.isEmpty()) {
            marketSymbolRepository.saveAll(changedSymbols);
        }
    }
}
