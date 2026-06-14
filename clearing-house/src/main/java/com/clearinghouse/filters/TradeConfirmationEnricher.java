package com.clearinghouse.filters;

import com.clearinghouse.domain.EnrichedConfirmation;
import com.clearinghouse.domain.NovatedTrade;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class TradeConfirmationEnricher {

    @Bean
    public Function<List<NovatedTrade>, List<EnrichedConfirmation>> enrich() {
        return novatedTrades -> novatedTrades.stream()
            .map(trade -> new EnrichedConfirmation(
                trade.tradeId(),
                trade.counterparty(),
                trade.clearingHouseId(),
                trade.amount(),
                trade.currency(),
                trade.settlementDate(),
                getCurrencyName(trade.currency()),
                getSettlementLocation(trade.currency())
            ))
            .toList();
    }

    private String getCurrencyName(String currency) {
        return switch(currency) {
            case "USD" -> "US Dollar";
            case "EUR" -> "Euro";
            case "GBP" -> "British Pound";
            default -> currency;
        };
    }

    private String getSettlementLocation(String currency) {
        return switch(currency) {
            case "USD" -> "New York";
            case "EUR" -> "Frankfurt";
            case "GBP" -> "London";
            default -> "Unknown";
        };
    }
}
