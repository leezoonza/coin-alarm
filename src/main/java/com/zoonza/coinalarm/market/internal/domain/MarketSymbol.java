package com.zoonza.coinalarm.market.internal.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_market_symbol_code_market_type",
                columnNames = {"code", "market_type"}
        )
})
public class MarketSymbol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MarketType marketType;

    @Column(nullable = false)
    private String baseAsset;

    @Column(nullable = false)
    private String quoteAsset;

    @Column(nullable = false, precision = 16, scale = 8)
    private BigDecimal tickSize;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant registeredAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private MarketSymbol(
            String code,
            MarketType marketType,
            String baseAsset,
            String quoteAsset,
            BigDecimal tickSize,
            String status,
            Instant registeredAt,
            Instant updatedAt
    ) {
        this.code = code;
        this.marketType = marketType;
        this.baseAsset = baseAsset;
        this.quoteAsset = quoteAsset;
        this.tickSize = tickSize;
        this.status = status;
        this.registeredAt = registeredAt;
        this.updatedAt = updatedAt;
    }

    public static MarketSymbol register(
            String code,
            MarketType marketType,
            String baseAsset,
            String quoteAsset,
            BigDecimal tickSize,
            String status,
            Instant registeredAt
    ) {
        return new MarketSymbol(
                code,
                marketType,
                baseAsset,
                quoteAsset,
                tickSize,
                status,
                registeredAt,
                registeredAt
        );
    }

    public boolean synchronize(
            String baseAsset,
            String quoteAsset,
            BigDecimal tickSize,
            String status,
            Instant updatedAt
    ) {
        boolean changed = !Objects.equals(this.baseAsset, baseAsset)
                        || !Objects.equals(this.quoteAsset, quoteAsset)
                        || !hasSameValue(this.tickSize, tickSize)
                        || !Objects.equals(this.status, status);

        if (!changed) {
            return false;
        }

        this.baseAsset = baseAsset;
        this.quoteAsset = quoteAsset;
        this.tickSize = tickSize;
        this.status = status;
        this.updatedAt = updatedAt;

        return true;
    }

    private boolean hasSameValue(
            BigDecimal current,
            BigDecimal incoming
    ) {
        if (current == null || incoming == null) {
            return current == incoming;
        }

        return current.compareTo(incoming) == 0;
    }
}
