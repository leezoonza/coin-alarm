package com.zoonza.coinalarm.market.internal.application.dto;

public record MarketSymbolSyncResult(
        int registeredCount,
        int updatedCount,
        int unchangedCount
) {
}
