package com.clearinghouse.filters;

import com.clearinghouse.domain.NovatedTrade;
import com.clearinghouse.domain.ValidatedTrade;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class TradeNovationTest {

    private final TradeNovation tradeNovation = new TradeNovation();

    @Test
    void shouldSplitTradeIntoTwoLegs() {
        // Given
        ValidatedTrade trade = new ValidatedTrade(
            "T-001",
            "Bank A",
            "Bank B",
            new BigDecimal("1000000.00"),
            "USD",
            LocalDate.of(2026, 6, 20)
        );

        // When
        var novateFunction = tradeNovation.novate();
        List<NovatedTrade> novatedTrades = novateFunction.apply(trade);

        // Then: Should create two trades
        assertThat(novatedTrades)
            .hasSize(2)
            .allMatch(t -> t.originalTradeId().equals(trade.tradeId()))
            .allMatch(t -> t.clearingHouseId().equals("CH-001"))
            .allMatch(t -> t.amount().equals(trade.amount()))
            .allMatch(t -> t.currency().equals(trade.currency()));

        // And: Counterparties should be separated
        var counterparties = novatedTrades.stream()
            .map(NovatedTrade::counterparty)
            .distinct()
            .toList();
        assertThat(counterparties)
            .hasSize(2)
            .contains("Bank A", "Bank B");
    }

    @Test
    void shouldInvariant_EachLegHasOriginalAmount() {
        // Given
        ValidatedTrade trade = new ValidatedTrade(
            "T-MATH",
            "Counterparty1",
            "Counterparty2",
            new BigDecimal("500000.50"),
            "EUR",
            LocalDate.of(2026, 7, 1)
        );

        // When
        var novateFunction = tradeNovation.novate();
        List<NovatedTrade> novated = novateFunction.apply(trade);

        // Then: Each novated leg should have the full original amount (standard clearing house behavior)
        assertThat(novated)
            .allMatch(t -> t.amount().compareTo(trade.amount()) == 0);
    }

    @Test
    void shouldInvariant_BothCounterpartiesPresent() {
        // Given
        ValidatedTrade trade = new ValidatedTrade(
            "T-BOTH",
            "Alice",
            "Bob",
            new BigDecimal("250000"),
            "GBP",
            LocalDate.of(2026, 7, 15)
        );

        // When
        var novateFunction = tradeNovation.novate();
        List<NovatedTrade> novated = novateFunction.apply(trade);

        // Then: Both original counterparties should appear
        assertThat(novated)
            .anyMatch(t -> t.counterparty().equals("Alice"))
            .anyMatch(t -> t.counterparty().equals("Bob"));
    }

    @Test
    void shouldGenerateUniqueTradeIds() {
        // Given
        ValidatedTrade trade = new ValidatedTrade(
            "T-IDS",
            "Party1",
            "Party2",
            new BigDecimal("100000"),
            "USD",
            LocalDate.of(2026, 8, 1)
        );

        // When: Generate multiple trades
        var novateFunction = tradeNovation.novate();
        List<NovatedTrade> batch1 = novateFunction.apply(trade);

        ValidatedTrade trade2 = new ValidatedTrade(
            "T-IDS2",
            "Party3",
            "Party4",
            new BigDecimal("200000"),
            "EUR",
            LocalDate.of(2026, 8, 2)
        );
        List<NovatedTrade> batch2 = novateFunction.apply(trade2);

        // Then: All trade IDs should be unique
        var allIds = new java.util.ArrayList<>(batch1.stream()
            .map(NovatedTrade::tradeId)
            .toList());
        allIds.addAll(batch2.stream()
            .map(NovatedTrade::tradeId)
            .toList());

        long uniqueIds = allIds.stream().distinct().count();
        assertThat(uniqueIds).isEqualTo(allIds.size());
    }
}
