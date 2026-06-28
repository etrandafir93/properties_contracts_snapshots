package com.clearinghouse;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping("/api/currencies")
class CurrencyController {

	private final JsonMapper jsonMapper;

	private Map<String, CurrencyDto> currencies;

	@GetMapping
	List<CurrencyDto> getAllCurrencies() {
		return currencies.values().stream()
				.sorted(Comparator.comparing(CurrencyDto::code)).toList();
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

	private Map<String, CurrencyDto> loadCurrencies() {
		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream("currencies.json");

		List<CurrencyDto> currencies = jsonMapper.readValue(
				inputStream, new TypeReference<>() {});

		return currencies.stream()
				.collect(toMap(CurrencyDto::code, identity()));
	}

	CurrencyController(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}
}
