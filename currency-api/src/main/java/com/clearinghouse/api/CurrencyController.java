package com.clearinghouse.api;

import com.clearinghouse.domain.Currency;
import com.clearinghouse.service.CurrencyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    public List<Currency> getAllCurrencies() {
        return currencyService.getAllCurrencies();
    }

    @GetMapping("/{code}")
    public Currency getCurrency(@PathVariable String code) {
        Currency currency = currencyService.getCurrency(code);
        if (currency == null) {
            throw new CurrencyNotFoundException("Currency not found: " + code);
        }
        return currency;
    }

    @ExceptionHandler(CurrencyNotFoundException.class)
    public ErrorResponse handleNotFound(CurrencyNotFoundException ex) {
        return new ErrorResponse(404, ex.getMessage());
    }

    private record ErrorResponse(int status, String message) {}

    public static class CurrencyNotFoundException extends RuntimeException {
        public CurrencyNotFoundException(String message) {
            super(message);
        }
    }
}
