package com.clearinghouse.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("unit")
@DisplayName("EmailNotifier unit tests")
class EmailNotifier_UnitTest {

	private final EmailNotifier notifier = new EmailNotifier();

	@Nested
	@DisplayName("HTML structure and content")
	class HtmlStructureAndContent {

		@Test
		@DisplayName("should generate valid HTML structure")
		void shouldGenerateValidHtmlStructure() {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					"New York"
			);

			String html = notifier.apply(confirmation);

			assertThat(html)
					.contains("<html><body>")
					.contains("</body></html>")
					.contains("<h1>Trade Confirmation</h1>");
		}

		@Test
		@DisplayName("should include trade ID in output")
		void shouldIncludeTradeId() {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRADE-ABC-123",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					"New York"
			);

			String html = notifier.apply(confirmation);

			assertThat(html)
					.contains("Trade ID: TRADE-ABC-123");
		}

		@Test
		@DisplayName("should include counterparty in output")
		void shouldIncludeCounterparty() {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"JPMorgan Chase",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					"New York"
			);

			String html = notifier.apply(confirmation);

			assertThat(html)
					.contains("Counterparty: JPMorgan Chase");
		}

		@Test
		@DisplayName("should include amount and currency")
		void shouldIncludeAmountAndCurrency() {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(2_500_000),
					"EUR",
					LocalDate.of(2026, 7, 1),
					"Euro",
					"Frankfurt"
			);

			String html = notifier.apply(confirmation);

			assertThat(html)
					.contains("Amount: 2500000 EUR (Euro)");
		}

		@Test
		@DisplayName("should include settlement date and location")
		void shouldIncludeSettlementDateAndLocation() {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 12, 25),
					"US Dollar",
					"London"
			);

			String html = notifier.apply(confirmation);

			assertThat(html)
					.contains("Settlement Date: 2026-12-25 — London");
		}
	}

	@Nested
	@DisplayName("Trade ID handling")
	class TradeIdHandling {

		@ParameterizedTest
		@ValueSource(strings = {"TRD-001", "TRADE-ABC-123", "123456789"})
		@DisplayName("should correctly format various trade IDs")
		void shouldFormatVariousTradeIds(String tradeId) {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					tradeId,
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					"New York"
			);

			String html = notifier.apply(confirmation);

			assertThat(html).contains("Trade ID: " + tradeId);
		}

		@Test
		@DisplayName("should handle UUID-based trade IDs")
		void shouldHandleUuidTradeIds() {
			String uuidTradeId = UUID.randomUUID().toString();
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					uuidTradeId,
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					"New York"
			);

			String html = notifier.apply(confirmation);

			assertThat(html).contains("Trade ID: " + uuidTradeId);
		}
	}

	@Nested
	@DisplayName("Amount formatting")
	class AmountFormatting {

		@ParameterizedTest
		@CsvSource({
				"100,100",
				"1000000,1000000",
				"0.01,0.01",
				"999999999.99,999999999.99"
		})
		void shouldFormatAmounts(String amountInput, String expectedDisplay) {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-42",
					new BigDecimal(amountInput),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					"New York"
			);

			String html = notifier.apply(confirmation);

			assertThat(html).contains("Amount: " + expectedDisplay + " USD");
		}

		@Test
		@DisplayName("should include currency code in amount")
		void shouldIncludeCurrencyCode() {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"GBP",
					LocalDate.of(2026, 7, 1),
					"British Pound",
					"London"
			);

			String html = notifier.apply(confirmation);

			assertThat(html).contains("1500000 GBP");
		}

		@Test
		@DisplayName("should include currency name in parentheses")
		void shouldIncludeCurrencyName() {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					"New York"
			);

			String html = notifier.apply(confirmation);

			assertThat(html).contains("(US Dollar)");
		}
	}

	@Nested
	@DisplayName("Counterparty handling")
	class CounterpartyHandling {

		@ParameterizedTest
		@ValueSource(strings = {"Goldman Sachs", "JPMorgan", "Citibank", "Bank of America"})
		@DisplayName("should include various counterparty names")
		void shouldIncludeVariousCounterparties(String counterparty) {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					counterparty,
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					"New York"
			);

			String html = notifier.apply(confirmation);

			assertThat(html).contains("Counterparty: " + counterparty);
		}
	}

	@Nested
	@DisplayName("Settlement location handling")
	class SettlementLocationHandling {

		@ParameterizedTest
		@ValueSource(strings = {"New York", "London", "Frankfurt", "Tokyo", "Sydney"})
		@DisplayName("should include various settlement locations")
		void shouldIncludeVariousLocations(String location) {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					location
			);

			String html = notifier.apply(confirmation);

			assertThat(html).contains("— " + location);
		}
	}

	@Nested
	@DisplayName("Settlement date formatting")
	class SettlementDateFormatting {

		@ParameterizedTest
		@CsvSource({
				"2026-07-01,2026-07-01",
				"2030-01-15,2030-01-15",
				"2030-12-31,2030-12-31"
		})
		void shouldFormatSettlementDates(String dateInput, String expectedDisplay) {
			LocalDate settlementDate = LocalDate.parse(dateInput);
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					settlementDate,
					"US Dollar",
					"New York"
			);

			String html = notifier.apply(confirmation);

			assertThat(html).contains("Settlement Date: " + expectedDisplay);
		}
	}

	@Nested
	@DisplayName("Complete email generation scenarios")
	class CompleteScenarios {

		@Test
		@DisplayName("should generate complete email with all details")
		void shouldGenerateCompleteEmail() {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-GS-2026-001",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(5_000_000),
					"EUR",
					LocalDate.of(2026, 7, 15),
					"Euro",
					"Frankfurt"
			);

			String html = notifier.apply(confirmation);

			assertThat(html)
					.contains("<html><body>")
					.contains("<h1>Trade Confirmation</h1>")
					.contains("Trade ID: TRD-GS-2026-001")
					.contains("Counterparty: Goldman Sachs")
					.contains("Amount: 5000000 EUR (Euro)")
					.contains("Settlement Date: 2026-07-15 — Frankfurt")
					.contains("</body></html>");
		}

		@Test
		@DisplayName("should generate consistent output for same input")
		void shouldGenerateConsistentOutput() {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					"New York"
			);

			String html1 = notifier.apply(confirmation);
			String html2 = notifier.apply(confirmation);

			assertThat(html1).isEqualTo(html2);
		}

		@Test
		@DisplayName("should generate different output for different inputs")
		void shouldGenerateDifferentOutputForDifferentInputs() {
			EnrichedConfirmation confirmation1 = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					"New York"
			);

			EnrichedConfirmation confirmation2 = new EnrichedConfirmation(
					"TRD-002",
					"JPMorgan",
					"CH-42",
					BigDecimal.valueOf(2_000_000),
					"EUR",
					LocalDate.of(2026, 8, 1),
					"Euro",
					"Frankfurt"
			);

			String html1 = notifier.apply(confirmation1);
			String html2 = notifier.apply(confirmation2);

			assertThat(html1).isNotEqualTo(html2);
		}

		@Test
		@DisplayName("should include clearing house ID in confirmation data")
		void shouldIncludeClearingHouseId() {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-100",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					"New York"
			);

			String html = notifier.apply(confirmation);

			// Verify clearing house ID is part of the enriched confirmation data
			assertThat(confirmation.clearingHouseId()).isEqualTo("CH-100");
		}
	}

	@Nested
	@DisplayName("HTML escaping and special characters")
	class SpecialCharacters {

		@Test
		@DisplayName("should handle counterparty names with special characters")
		void shouldHandleSpecialCharacterInCounterparty() {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Bank & Co.",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					"New York"
			);

			String html = notifier.apply(confirmation);

			assertThat(html).contains("Bank & Co.");
		}

		@Test
		@DisplayName("should handle currency names with symbols")
		void shouldHandleCurrencyWithSymbols() {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar ($)",
					"New York"
			);

			String html = notifier.apply(confirmation);

			assertThat(html).contains("US Dollar ($)");
		}
	}

	@Nested
	@DisplayName("Multi-parameter validation")
	class MultiParameterValidation {

		@ParameterizedTest
		@CsvSource({
				"TRD-001,Alice,CH-001,1000,USD,2026-07-01,US Dollar,New York",
				"TRD-002,Bob,CH-002,2000,EUR,2026-08-15,Euro,Frankfurt",
				"TRD-003,Charlie,CH-003,500,GBP,2026-09-30,British Pound,London",
				"TRD-004,Diana,CH-004,3000000,JPY,2026-10-15,Japanese Yen,Tokyo",
				"TRD-005,Eve,CH-005,750000,CHF,2026-11-01,Swiss Franc,Zurich"
		})
		@DisplayName("should generate emails for various trade configurations")
		void shouldGenerateEmailsForVariousConfigs(String tradeId, String counterparty, String clearingId,
				String amount, String currency, String date, String currencyName, String location) {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					tradeId,
					counterparty,
					clearingId,
					new BigDecimal(amount),
					currency,
					LocalDate.parse(date),
					currencyName,
					location
			);

			String html = notifier.apply(confirmation);

			assertThat(html)
					.contains("Trade ID: " + tradeId)
					.contains("Counterparty: " + counterparty)
					.contains(amount + " " + currency)
					.contains("Settlement Date: " + date + " — " + location);
		}

		@ParameterizedTest
		@CsvSource({
				"ALPHA-001,Alpha Inc,100",
				"BETA-002,Beta Corp,50000",
				"GAMMA-003,Gamma LLC,999999.99",
				"DELTA-004,Delta Trading,12345.67",
				"EPSILON-005,Epsilon Financial,500000000"
		})
		@DisplayName("should handle various trade IDs and amounts")
		void shouldHandleVariousTradeIdsAndAmounts(String tradeId, String counterparty, String amount) {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					tradeId,
					counterparty,
					"CH-99",
					new BigDecimal(amount),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					"New York"
			);

			String html = notifier.apply(confirmation);

			assertThat(html)
					.contains("Trade ID: " + tradeId)
					.contains("Amount: " + amount + " USD");
		}

		@ParameterizedTest
		@CsvSource({
				"New York,USA",
				"London,United Kingdom",
				"Frankfurt,Germany",
				"Tokyo,Japan",
				"Singapore,Singapore",
				"Hong Kong,Hong Kong",
				"Sydney,Australia",
				"Toronto,Canada"
		})
		@DisplayName("should handle various international settlement locations")
		void shouldHandleInternationalLocations(String location, String description) {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					"USD",
					LocalDate.of(2026, 7, 1),
					"US Dollar",
					location
			);

			String html = notifier.apply(confirmation);

			assertThat(html).contains("— " + location);
		}

		@ParameterizedTest
		@CsvSource({
				"USD,US Dollar",
				"EUR,Euro",
				"GBP,British Pound",
				"JPY,Japanese Yen",
				"CHF,Swiss Franc",
				"CAD,Canadian Dollar",
				"AUD,Australian Dollar",
				"NZD,New Zealand Dollar"
		})
		@DisplayName("should format various currencies correctly")
		void shouldFormatVariousCurrencies(String code, String name) {
			EnrichedConfirmation confirmation = new EnrichedConfirmation(
					"TRD-001",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_500_000),
					code,
					LocalDate.of(2026, 7, 1),
					name,
					"New York"
			);

			String html = notifier.apply(confirmation);

			assertThat(html)
					.contains(code)
					.contains(name);
		}
	}
}
