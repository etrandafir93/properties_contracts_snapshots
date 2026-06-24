package com.clearinghouse.notification;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.clearinghouse.Filter;

import com.clearinghouse.notification.CurrencyApiClient.CurrencyDto;
import com.clearinghouse.novation.NovatedTrade;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Function;

@Filter("notification-enrich")
@Slf4j
@RequiredArgsConstructor
class TradeConfirmationEnricher implements Function<NovatedTrade, EnrichedConfirmation> {

    private final CurrencyApiClient currencyApiClient;
    private Map<String, CurrencyDto> currencyCache;

    @PostConstruct
    void loadCurrencies() {
        log.info("Loading currency cache from currency-api...");
        currencyCache = currencyApiClient.fetchAll()
                .stream()
                .collect(toMap(CurrencyDto::code, identity()));
        log.info("Currency cache loaded with {} currencies", currencyCache.size());
    }

    @Override
    public EnrichedConfirmation apply(NovatedTrade trade) {
        log.info("[notification-enrich] Enriching trade confirmation: {}", trade.tradeId());
        CurrencyDto dto = currencyCache.get(trade.currency());
        EnrichedConfirmation enriched = new EnrichedConfirmation(
                trade.tradeId(),
                trade.counterparty(),
                trade.clearingHouseId(),
                trade.amount(),
                trade.currency(),
                trade.settlementDate(),
                displayName(dto),
                dto.settlementLocation()
        );
        log.info("[notification-enrich] Trade confirmation enriched: {}", enriched.tradeId());
        return enriched;
    }

    private String displayName(CurrencyDto dto) {
		String code = dto.isoNumericCode() > 900
				? dto.isoNumericCode().toString()
				: dto.code();
		return "%s (%s)".formatted(dto.name(), code);
    }
}
