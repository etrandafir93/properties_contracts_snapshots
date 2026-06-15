package com.clearinghouse.email;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
class CurrencyCache {

    private final CurrencyApiClient currencyApiClient;
    private Map<String, CurrencyApiClient.CurrencyDto> cache;

    @PostConstruct
    void load() {
        log.info("Loading currency cache from currency-api...");
        cache = currencyApiClient.fetchAll().stream()
                .collect(toMap(CurrencyApiClient.CurrencyDto::code, identity()));
        log.info("Currency cache loaded with {} currencies", cache.size());
    }

    CurrencyApiClient.CurrencyDto get(String code) {
        return cache.get(code);
    }
}
