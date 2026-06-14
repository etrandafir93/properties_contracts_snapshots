package com.clearinghouse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/currencies")
class CurrencyController {

    private final Map<String, CurrencyDto> currencies;

    CurrencyController() {
        this.currencies = loadCurrencies();
    }

    @GetMapping
    List<CurrencyDto> getAllCurrencies() {
        return currencies.values().stream().toList();
    }

    @GetMapping("/{code}")
    CurrencyDto getCurrency(@PathVariable String code) {
        CurrencyDto currency = currencies.get(code.toUpperCase());
        if (currency == null) {
            throw new CurrencyNotFoundException("Currency not found: " + code);
        }
        return currency;
    }

    @ExceptionHandler(CurrencyNotFoundException.class)
    ErrorResponse handleNotFound(CurrencyNotFoundException ex) {
        return new ErrorResponse(404, ex.getMessage());
    }

    private record ErrorResponse(int status, String message) {}

    static class CurrencyNotFoundException extends RuntimeException {
        CurrencyNotFoundException(String message) {
            super(message);
        }
    }

    private Map<String, CurrencyDto> loadCurrencies() {
        Map<String, CurrencyDto> result = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("currencies.json")) {
            if (inputStream != null) {
                List<CurrencyEntity> entities = mapper.readValue(inputStream, new TypeReference<>() {});
                entities.forEach(e -> result.put(e.code(), CurrencyDto.from(e)));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load currencies", e);
        }

        return result;
    }
}
