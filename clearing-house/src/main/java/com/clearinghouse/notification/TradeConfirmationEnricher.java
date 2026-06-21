package com.clearinghouse.notification;

import com.clearinghouse.application.Filter;
import com.clearinghouse.application.LogUtils;
import com.clearinghouse.notification.CurrencyApiClient.CurrencyDto;
import com.clearinghouse.novation.NovatedTrade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Filter("notification-enrich")
@Slf4j
@RequiredArgsConstructor
class TradeConfirmationEnricher implements Function<NovatedTrade, EnrichedConfirmation> {

    private final CurrencyCache currencyCache;

    @Override
    public EnrichedConfirmation apply(NovatedTrade trade) {
        log.info("{}[notification-enrich] Enriching trade confirmation: {}{}", LogUtils.INDIGO, trade.tradeId(), LogUtils.RESET);
        CurrencyDto currency = currencyCache.get(trade.currency());
        EnrichedConfirmation enriched = new EnrichedConfirmation(
                trade.tradeId(),
                trade.counterparty(),
                trade.clearingHouseId(),
                trade.amount(),
                trade.currency(),
                trade.settlementDate(),
                displayName(currency),
                currency.settlementLocation()
        );
        log.info("{}[notification-enrich] Trade confirmation enriched: {}{}", LogUtils.INDIGO, enriched.tradeId(), LogUtils.RESET);
        return enriched;
    }

    private String displayName(CurrencyDto currency) {
        return "%s (%s)".formatted(currency.name(), currency.code());
    }
}
