package com.clearinghouse.email;

import com.clearinghouse.Filter;
import com.clearinghouse.LogUtils;
import com.clearinghouse.novation.NovatedTrade;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Filter("enrich")
@Slf4j
class TradeConfirmationEnricher implements Function<NovatedTrade, EnrichedConfirmation> {

    @Override
    public EnrichedConfirmation apply(NovatedTrade trade) {
        log.info("{}[enrich] Enriching trade confirmation: {}{}", LogUtils.INDIGO, trade.tradeId(), LogUtils.RESET);
        EnrichedConfirmation enriched = new EnrichedConfirmation(
            trade.tradeId(),
            trade.counterparty(),
            trade.clearingHouseId(),
            trade.amount(),
            trade.currency(),
            trade.settlementDate(),
            getCurrencyName(trade.currency()),
            getSettlementLocation(trade.currency())
        );
        log.info("{}[enrich] Trade confirmation enriched: {}{}", LogUtils.INDIGO, enriched.tradeId(), LogUtils.RESET);
        return enriched;
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
