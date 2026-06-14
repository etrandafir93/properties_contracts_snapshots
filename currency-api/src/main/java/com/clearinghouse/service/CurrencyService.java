package com.clearinghouse.service;

import com.clearinghouse.domain.Currency;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService {

    private final Map<String, Currency> currencies;

    public CurrencyService() {
        this.currencies = loadCurrencies();
    }

    public Currency getCurrency(String code) {
        return currencies.get(code.toUpperCase());
    }

    public List<Currency> getAllCurrencies() {
        return currencies.values().stream().toList();
    }

    private Map<String, Currency> loadCurrencies() {
        Map<String, Currency> result = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("currencies.json")) {
            if (inputStream != null) {
                List<Currency> currencyList = mapper.readValue(
                    inputStream,
                    new TypeReference<List<Currency>>() {}
                );
                currencyList.forEach(c -> result.put(c.code(), c));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load currencies", e);
        }

        return result;
    }
}
