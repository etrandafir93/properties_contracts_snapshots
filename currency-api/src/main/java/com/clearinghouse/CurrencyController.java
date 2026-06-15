package com.clearinghouse;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/currencies")
class CurrencyController {

	private Map<String, Currency> currencies;

	@GetMapping
	List<Currency> getAllCurrencies() {
		return currencies.values().stream().toList();
	}

	@GetMapping("/{code}")
	Currency getCurrency(@PathVariable String code) {
		Currency currency = currencies.get(code.toUpperCase());
		if (currency == null) {
			throw new CurrencyNotFoundException("Currency not found: " + code);
		}
		return currency;
	}

	@ExceptionHandler(CurrencyNotFoundException.class)
	ErrorResponse handleNotFound(CurrencyNotFoundException ex) {
		return new ErrorResponse(404, ex.getMessage());
	}

	private record ErrorResponse(int status, String message) {
	}

	static class CurrencyNotFoundException extends RuntimeException {
		CurrencyNotFoundException(String message) {
			super(message);
		}
	}

	@PostConstruct
	void load() {
		this.currencies = loadCurrencies();
	}

	private Map<String, Currency> loadCurrencies() {
		Map<String, Currency> result = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		try (InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream("currencies.json")) {
			if (inputStream != null) {
				List<Currency> currencies = mapper.readValue(inputStream,
						new TypeReference<>() {
						});
				currencies.forEach(c -> result.put(c.code(), c));
			}
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to load currencies", e);
		}

		return result;
	}
}
