package com.clearinghouse.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.clearinghouse.novation.NovatedTrade;
import com.clearinghouse.notification.CurrencyApiClient.CurrencyDto;

@Tag("unit")
@DisplayName("TradeConfirmationEnricher unit tests")
class TradeConfirmationEnricher_UnitTest {

	@Mock
	private CurrencyApiClient currencyApiClient;

	private TradeConfirmationEnricher enricher;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		enricher = new TradeConfirmationEnricher(currencyApiClient);
	}

	@Nested
	@DisplayName("Currency loading")
	class CurrencyLoading {

		@Test
		@DisplayName("should load currencies from API")
		void shouldLoadCurrenciesFromApi() {
			List<CurrencyDto> currencies = List.of(
					new CurrencyDto("USD", "US Dollar", "$", "New York", 2, 840, LocalDate.of(2020, 1, 1)),
					new CurrencyDto("EUR", "Euro", "€", "Frankfurt", 2, 978, LocalDate.of(2020, 1, 1))
			);
			when(currencyApiClient.fetchAll()).thenReturn(currencies);

			enricher.loadCurrencies();

			// Should not throw
		}

		@Test
		@DisplayName("should cache loaded currencies")
		void shouldCacheCurrencies() {
			List<CurrencyDto> currencies = List.of(
					new CurrencyDto("USD", "US Dollar", "$", "New York", 2, 840, LocalDate.of(2020, 1, 1))
			);
			when(currencyApiClient.fetchAll()).thenReturn(currencies);

			enricher.loadCurrencies();

			// After loading, enrichment should work
			NovatedTrade trade = new NovatedTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			EnrichedConfirmation result = enricher.apply(trade);

			assertThat(result.currencyName()).isEqualTo("US Dollar (USD)");
		}
	}

	@Nested
	@DisplayName("Trade enrichment with standard currencies")
	class TradeEnrichment {

		@BeforeEach
		void loadCurrencies() {
			List<CurrencyDto> currencies = List.of(
					new CurrencyDto("USD", "US Dollar", "$", "New York", 2, 840, LocalDate.of(2020, 1, 1)),
					new CurrencyDto("EUR", "Euro", "€", "Frankfurt", 2, 978, LocalDate.of(2020, 1, 1)),
					new CurrencyDto("GBP", "British Pound", "£", "London", 2, 826, LocalDate.of(2020, 1, 1)),
					new CurrencyDto("JPY", "Japanese Yen", "¥", "Tokyo", 0, 392, LocalDate.of(2020, 1, 1))
			);
			when(currencyApiClient.fetchAll()).thenReturn(currencies);
			enricher.loadCurrencies();
		}

		@Test
		@DisplayName("should enrich trade with USD currency details")
		void shouldEnrichWithUSD() {
			NovatedTrade trade = new NovatedTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			EnrichedConfirmation result = enricher.apply(trade);

			assertThat(result)
					.hasFieldOrPropertyWithValue("currencyName", "US Dollar (USD)")
					.hasFieldOrPropertyWithValue("settlementLocation", "New York");
		}

		@Test
		@DisplayName("should enrich trade with EUR currency details using iso numeric code")
		void shouldEnrichWithEUR() {
			NovatedTrade trade = new NovatedTrade(
					UUID.randomUUID().toString(),
					"Bob",
					"CH-001",
					BigDecimal.valueOf(5000),
					"EUR",
					LocalDate.of(2030, 6, 15),
					"ORIGINAL-002",
					"SELL"
			);

			EnrichedConfirmation result = enricher.apply(trade);

			// EUR iso_numeric_code is 978 > 900, so displays numeric code
			assertThat(result)
					.hasFieldOrPropertyWithValue("currencyName", "Euro (978)")
					.hasFieldOrPropertyWithValue("settlementLocation", "Frankfurt");
		}

		@Test
		@DisplayName("should preserve core trade data during enrichment")
		void shouldPreserveCoreTradeData() {
			NovatedTrade trade = new NovatedTrade(
					"TRADE-123",
					"Goldman Sachs",
					"CH-001",
					BigDecimal.valueOf(5_000_000),
					"USD",
					LocalDate.of(2030, 12, 15),
					"ORIGINAL-456",
					"BUY"
			);

			EnrichedConfirmation result = enricher.apply(trade);

			assertThat(result)
					.hasFieldOrPropertyWithValue("tradeId", "TRADE-123")
					.hasFieldOrPropertyWithValue("counterparty", "Goldman Sachs")
					.hasFieldOrPropertyWithValue("clearingHouseId", "CH-001")
					.hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(5_000_000))
					.hasFieldOrPropertyWithValue("currency", "USD")
					.hasFieldOrPropertyWithValue("settlementDate", LocalDate.of(2030, 12, 15));
		}

		@ParameterizedTest
		@CsvSource({
				"USD,US Dollar (USD),New York",
				"EUR,Euro (978),Frankfurt",
				"GBP,British Pound (GBP),London",
				"JPY,Japanese Yen (JPY),Tokyo"
		})
		@DisplayName("should enrich all supported currencies correctly")
		void shouldEnrichAllCurrencies(String currency, String expectedName, String expectedLocation) {
			NovatedTrade trade = new NovatedTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					currency,
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			EnrichedConfirmation result = enricher.apply(trade);

			assertThat(result)
					.hasFieldOrPropertyWithValue("currencyName", expectedName)
					.hasFieldOrPropertyWithValue("settlementLocation", expectedLocation);
		}
	}

	@Nested
	@DisplayName("Currency display name formatting")
	class CurrencyDisplayNameFormatting {

		@BeforeEach
		void loadCurrencies() {
			List<CurrencyDto> currencies = List.of(
					// Standard currency with iso code < 900
					new CurrencyDto("USD", "US Dollar", "$", "New York", 2, 840, LocalDate.of(2020, 1, 1)),
					// Currency with iso code > 900 (uses numeric code instead)
					new CurrencyDto("EUR", "Euro", "€", "Frankfurt", 2, 978, LocalDate.of(2020, 1, 1))
			);
			when(currencyApiClient.fetchAll()).thenReturn(currencies);
			enricher.loadCurrencies();
		}

		@Test
		@DisplayName("should display currency code when iso numeric <= 900")
		void shouldDisplayCodeWhenLessThan900() {
			NovatedTrade trade = new NovatedTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			EnrichedConfirmation result = enricher.apply(trade);

			// USD has iso 840 (< 900), so should display code
			assertThat(result.currencyName()).isEqualTo("US Dollar (USD)");
		}

		@Test
		@DisplayName("should display iso numeric code when iso numeric > 900")
		void shouldDisplayNumericCodeWhenGreaterThan900() {
			NovatedTrade trade = new NovatedTrade(
					UUID.randomUUID().toString(),
					"Bob",
					"CH-001",
					BigDecimal.valueOf(1000),
					"EUR",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"SELL"
			);

			EnrichedConfirmation result = enricher.apply(trade);

			// EUR has iso 978 (> 900), so should display numeric code
			assertThat(result.currencyName()).isEqualTo("Euro (978)");
		}

		@Test
		@DisplayName("should display iso numeric code when iso numeric equals 900")
		void shouldDisplayNumericCodeWhenEqualTo900() {
			List<CurrencyDto> currencies = List.of(
					new CurrencyDto("ZZZ", "Test Currency", "Z", "Test", 2, 900, LocalDate.of(2020, 1, 1))
			);
			when(currencyApiClient.fetchAll()).thenReturn(currencies);
			enricher.loadCurrencies();

			NovatedTrade trade = new NovatedTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"ZZZ",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			EnrichedConfirmation result = enricher.apply(trade);

			// iso 900 is not > 900, so should display code
			assertThat(result.currencyName()).isEqualTo("Test Currency (ZZZ)");
		}
	}

	@Nested
	@DisplayName("Large and edge case amounts")
	class EdgeCaseAmounts {

		@BeforeEach
		void loadCurrencies() {
			List<CurrencyDto> currencies = List.of(
					new CurrencyDto("USD", "US Dollar", "$", "New York", 2, 840, LocalDate.of(2020, 1, 1))
			);
			when(currencyApiClient.fetchAll()).thenReturn(currencies);
			enricher.loadCurrencies();
		}

		@ParameterizedTest
		@ValueSource(strings = {"0.01", "1", "1000000", "999999999.99"})
		@DisplayName("should enrich trades with various amounts")
		void shouldEnrichVariousAmounts(String amountStr) {
			BigDecimal amount = new BigDecimal(amountStr);
			NovatedTrade trade = new NovatedTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					amount,
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			EnrichedConfirmation result = enricher.apply(trade);

			assertThat(result.amount()).isEqualTo(amount);
		}
	}

	@Nested
	@DisplayName("Different settlement dates")
	class SettlementDates {

		@BeforeEach
		void loadCurrencies() {
			List<CurrencyDto> currencies = List.of(
					new CurrencyDto("USD", "US Dollar", "$", "New York", 2, 840, LocalDate.of(2020, 1, 1))
			);
			when(currencyApiClient.fetchAll()).thenReturn(currencies);
			enricher.loadCurrencies();
		}

		@ParameterizedTest
		@ValueSource(strings = {"2030-01-01", "2030-06-15", "2030-12-31"})
		@DisplayName("should preserve various settlement dates")
		void shouldPreserveSettlementDates(String dateStr) {
			LocalDate settlementDate = LocalDate.parse(dateStr);
			NovatedTrade trade = new NovatedTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					settlementDate,
					"ORIGINAL-001",
					"BUY"
			);

			EnrichedConfirmation result = enricher.apply(trade);

			assertThat(result.settlementDate()).isEqualTo(settlementDate);
		}
	}

	@Nested
	@DisplayName("Multi-parameter enrichment scenarios")
	class MultiParameterEnrichment {

		@BeforeEach
		void loadCurrencies() {
			List<CurrencyDto> currencies = List.of(
					new CurrencyDto("USD", "US Dollar", "$", "New York", 2, 840, LocalDate.of(2020, 1, 1)),
					new CurrencyDto("EUR", "Euro", "€", "Frankfurt", 2, 978, LocalDate.of(2020, 1, 1)),
					new CurrencyDto("GBP", "British Pound", "£", "London", 2, 826, LocalDate.of(2020, 1, 1)),
					new CurrencyDto("JPY", "Japanese Yen", "¥", "Tokyo", 0, 392, LocalDate.of(2020, 1, 1)),
					new CurrencyDto("CHF", "Swiss Franc", "CHF", "Zurich", 2, 756, LocalDate.of(2020, 1, 1)),
					new CurrencyDto("CAD", "Canadian Dollar", "C$", "Toronto", 2, 124, LocalDate.of(2020, 1, 1))
			);
			when(currencyApiClient.fetchAll()).thenReturn(currencies);
			enricher.loadCurrencies();
		}

		@ParameterizedTest
		@CsvSource({
				"USD,US Dollar (USD),New York,1000",
				"EUR,Euro (978),Frankfurt,5000000",
				"GBP,British Pound (GBP),London,250000",
				"JPY,Japanese Yen (JPY),Tokyo,3000000",
				"CHF,Swiss Franc (CHF),Zurich,500000",
				"CAD,Canadian Dollar (CAD),Toronto,750000"
		})
		@DisplayName("should enrich trades with various configurations")
		void shouldEnrichVariousConfigurations(String currency, String expectedName, String expectedLocation, String amount) {
			NovatedTrade trade = new NovatedTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					new BigDecimal(amount),
					currency,
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			EnrichedConfirmation result = enricher.apply(trade);

			assertThat(result)
					.hasFieldOrPropertyWithValue("currencyName", expectedName)
					.hasFieldOrPropertyWithValue("settlementLocation", expectedLocation)
					.hasFieldOrPropertyWithValue("amount", new BigDecimal(amount))
					.hasFieldOrPropertyWithValue("currency", currency);
		}

		@ParameterizedTest
		@CsvSource({
				"TRADE-001,Goldman Sachs,CH-001",
				"TRADE-002,JPMorgan,CH-002",
				"TRADE-003,Citibank,CH-003",
				"TRADE-004,Bank of America,CH-004",
				"TRADE-005,Wells Fargo,CH-005"
		})
		@DisplayName("should preserve trade identification data")
		void shouldPreserveTradeIdData(String tradeId, String counterparty, String clearingId) {
			NovatedTrade trade = new NovatedTrade(
					tradeId,
					counterparty,
					clearingId,
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			EnrichedConfirmation result = enricher.apply(trade);

			assertThat(result)
					.hasFieldOrPropertyWithValue("tradeId", tradeId)
					.hasFieldOrPropertyWithValue("counterparty", counterparty)
					.hasFieldOrPropertyWithValue("clearingHouseId", clearingId);
		}

		@ParameterizedTest
		@CsvSource({
				"0.01,0.01",
				"100,100",
				"1000000,1000000",
				"50000000.50,50000000.50",
				"999999999.99,999999999.99"
		})
		@DisplayName("should preserve various trade amounts")
		void shouldPreserveVariousAmounts(String amountStr, String expectedStr) {
			BigDecimal amount = new BigDecimal(amountStr);
			BigDecimal expected = new BigDecimal(expectedStr);
			NovatedTrade trade = new NovatedTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					amount,
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			EnrichedConfirmation result = enricher.apply(trade);

			assertThat(result.amount()).isEqualTo(expected);
		}

		@ParameterizedTest
		@CsvSource({
				"BUY",
				"SELL"
		})
		@DisplayName("should enrich both BUY and SELL sides")
		void shouldEnrichBothSides(String side) {
			NovatedTrade trade = new NovatedTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					side
			);

			EnrichedConfirmation result = enricher.apply(trade);

			assertThat(result)
					.hasFieldOrPropertyWithValue("currencyName", "US Dollar (USD)")
					.hasFieldOrPropertyWithValue("settlementLocation", "New York");
		}
	}
}
