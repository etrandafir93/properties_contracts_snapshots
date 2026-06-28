package com.clearinghouse.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
@DisplayName("RiskValidator unit tests")
class RiskValidator_UnitTest {

	// Fixed clock: Tuesday 2030-01-01 10:00 UTC
	// USD/EUR/GBP/JPY all have T+2 max settlement lag
	private static final Clock CLOCK =
			Clock.fixed(Instant.parse("2030-01-01T10:00:00Z"), ZoneOffset.UTC);
	private static final LocalDate TODAY = LocalDate.of(2030, 1, 1);
	private static final LocalDate TOMORROW = LocalDate.of(2030, 1, 2);

	private final RiskValidator validator = new RiskValidator(CLOCK);

	@Nested
	@DisplayName("Self-trade rejection (Rule 1)")
	class RejectSelfTrades {

		@Test
		@DisplayName("should reject when party equals counterparty")
		void shouldRejectSelfTrade() {
			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Alice",
					BigDecimal.valueOf(1000),
					"USD",
					TOMORROW
			);

			assertThatThrownBy(() -> validator.apply(trade))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Self-trade rejected")
					.hasMessageContaining("Alice");
		}

		@ParameterizedTest
		@ValueSource(strings = {"Alice", "Bob", "Charlie", "Alice123"})
		@DisplayName("should reject various self-trade scenarios")
		void shouldRejectDifferentSelfTrades(String party) {
			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					party,
					party,
					BigDecimal.valueOf(1000),
					"USD",
					TOMORROW
			);

			assertThatThrownBy(() -> validator.apply(trade))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Self-trade rejected");
		}

		@Test
		@DisplayName("should accept when parties differ")
		void shouldAcceptDifferentParties() {
			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					"USD",
					TOMORROW
			);

			ValidatedTrade result = validator.apply(trade);

			assertThat(result)
					.hasFieldOrPropertyWithValue("party", "Alice")
					.hasFieldOrPropertyWithValue("counterparty", "Bob");
		}

		@ParameterizedTest
		@CsvSource({
				"Alice,Bob",
				"Bob,Alice",
				"Party1,Party2",
				"SELLER,BUYER"
		})
		@DisplayName("should accept parameterized different parties")
		void shouldAcceptDifferentPartiesParameterized(String party, String counterparty) {
			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					party,
					counterparty,
					BigDecimal.valueOf(1000),
					"USD",
					TOMORROW
			);

			ValidatedTrade result = validator.apply(trade);

			assertThat(result)
					.hasFieldOrPropertyWithValue("party", party)
					.hasFieldOrPropertyWithValue("counterparty", counterparty);
		}
	}

	@Nested
	@DisplayName("Settlement date window validation (Rule 2)")
	class RejectOutsideSettlementWindow {

		// With USD/EUR/GBP/JPY T+2: valid window is [2030-01-01, 2030-01-03] (business days)
		// 2030-01-01 is Tuesday, 2030-01-02 is Wednesday, 2030-01-03 is Thursday

		@Test
		@DisplayName("should accept settlement on today")
		void shouldAcceptSettlementOnToday() {
			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					"USD",
					TODAY
			);

			ValidatedTrade result = validator.apply(trade);

			assertThat(result.settlementDate()).isEqualTo(TODAY);
		}

		@Test
		@DisplayName("should accept settlement at T+1")
		void shouldAcceptSettlementAtT1() {
			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					"USD",
					TOMORROW
			);

			ValidatedTrade result = validator.apply(trade);

			assertThat(result.settlementDate()).isEqualTo(TOMORROW);
		}

		@Test
		@DisplayName("should accept settlement at maximum allowed T+2")
		void shouldAcceptMaximumSettlement() {
			LocalDate maxSettlement = LocalDate.of(2030, 1, 3); // Thursday (T+2 business days)

			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					"USD",
					maxSettlement
			);

			ValidatedTrade result = validator.apply(trade);

			assertThat(result.settlementDate()).isEqualTo(maxSettlement);
		}

		@Test
		@DisplayName("should reject settlement beyond T+2")
		void shouldRejectBeyondMaxSettlement() {
			LocalDate tooLate = LocalDate.of(2030, 1, 4); // Friday (beyond T+2)

			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					"USD",
					tooLate
			);

			assertThatThrownBy(() -> validator.apply(trade))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("outside the allowed window");
		}

		@Test
		@DisplayName("should reject past settlement dates")
		void shouldRejectPastSettlement() {
			LocalDate pastDate = LocalDate.of(2029, 12, 31);

			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					"USD",
					pastDate
			);

			assertThatThrownBy(() -> validator.apply(trade))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("outside the allowed window");
		}

		@ParameterizedTest
		@CsvSource({
				"USD,2",
				"EUR,2",
				"GBP,2",
				"JPY,2"
		})
		@DisplayName("should accept configured max lag for each currency")
		void shouldAcceptMaxLagPerCurrency(String currency, int maxLagDays) {
			LocalDate maxSettlement = TODAY.plusDays(maxLagDays);

			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					currency,
					maxSettlement
			);

			ValidatedTrade result = validator.apply(trade);

			assertThat(result.currency()).isEqualTo(currency);
		}

		@Test
		@DisplayName("should reject unknown currency with default T+2")
		void shouldRejectUnknownCurrencyBeyondDefault() {
			LocalDate tooLate = LocalDate.of(2030, 1, 10);

			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					"XYZ", // Unknown currency
					tooLate
			);

			assertThatThrownBy(() -> validator.apply(trade))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("outside the allowed window");
		}
	}

	@Nested
	@DisplayName("Friday cutoff rolling (Rule 3)")
	class FridayAfterCutoffRolling {

		@Test
		@DisplayName("should not roll settlement before Friday 17:00")
		void shouldNotRollBeforeFridayEventWithFixedClock() {
			// Clock is Tuesday 10:00 — before Friday cutoff
			LocalDate requestedSettlement = LocalDate.of(2030, 1, 3); // Thursday

			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					"USD",
					requestedSettlement
			);

			ValidatedTrade result = validator.apply(trade);

			// Settlement should not be rolled since we're before Friday cutoff
			assertThat(result.settlementDate()).isEqualTo(requestedSettlement);
		}

		@Test
		@DisplayName("should roll settlement after Friday 17:00")
		void shouldRollSettlementAfterFridayCutoff() {
			// Clock is Friday 18:00 (after cutoff)
			Clock fridayAfterCutoff = Clock.fixed(
					Instant.parse("2030-01-04T18:00:00Z"), // Friday 6 PM
					ZoneOffset.UTC
			);
			RiskValidator validatorAfterCutoff = new RiskValidator(fridayAfterCutoff);

			LocalDate requestedSettlement = LocalDate.of(2030, 1, 4); // Friday

			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					"USD",
					requestedSettlement
			);

			ValidatedTrade result = validatorAfterCutoff.apply(trade);

			// Should roll to next Monday (2030-01-07)
			assertThat(result.settlementDate()).isEqualTo(LocalDate.of(2030, 1, 7));
		}

		@Test
		@DisplayName("should roll weekend settlement to next Monday")
		void shouldRollWeekendSettlement() {
			// Clock is Saturday
			Clock weekendClock = Clock.fixed(
					Instant.parse("2030-01-05T10:00:00Z"), // Saturday
					ZoneOffset.UTC
			);
			RiskValidator validatorWeekend = new RiskValidator(weekendClock);

			LocalDate requestedSettlement = LocalDate.of(2030, 1, 5); // Saturday

			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					"USD",
					requestedSettlement
			);

			ValidatedTrade result = validatorWeekend.apply(trade);

			// Should roll to Monday
			assertThat(result.settlementDate()).isEqualTo(LocalDate.of(2030, 1, 7));
		}

		@Test
		@DisplayName("should accept Monday settlement when rolling from weekend")
		void shouldAcceptMondayAfterRoll() {
			Clock sundayClock = Clock.fixed(
					Instant.parse("2030-01-06T10:00:00Z"), // Sunday
					ZoneOffset.UTC
			);
			RiskValidator validatorSunday = new RiskValidator(sundayClock);

			LocalDate requestedSettlement = LocalDate.of(2030, 1, 6); // Sunday

			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					"USD",
					requestedSettlement
			);

			ValidatedTrade result = validatorSunday.apply(trade);

			// Should roll to Monday
			assertThat(result.settlementDate()).isEqualTo(LocalDate.of(2030, 1, 7));
		}
	}

	@Nested
	@DisplayName("Combined rule validation")
	class CombinedValidation {

		@Test
		@DisplayName("should validate complete valid trade")
		void shouldValidateCompleteValidTrade() {
			IncomingTrade trade = new IncomingTrade(
					"TRADE-001",
					"Goldman Sachs",
					"JPMorgan",
					BigDecimal.valueOf(5_000_000),
					"USD",
					TOMORROW
			);

			ValidatedTrade result = validator.apply(trade);

			assertThat(result)
					.hasFieldOrPropertyWithValue("tradeId", "TRADE-001")
					.hasFieldOrPropertyWithValue("party", "Goldman Sachs")
					.hasFieldOrPropertyWithValue("counterparty", "JPMorgan")
					.hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(5_000_000))
					.hasFieldOrPropertyWithValue("currency", "USD")
					.hasFieldOrPropertyWithValue("settlementDate", TOMORROW);
		}

		@Test
		@DisplayName("should catch self-trade before settlement validation")
		void shouldCatchSelfTradeFirst() {
			LocalDate invalidSettlement = LocalDate.of(2030, 12, 31);

			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Alice",
					BigDecimal.valueOf(1000),
					"USD",
					invalidSettlement
			);

			assertThatThrownBy(() -> validator.apply(trade))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Self-trade rejected");
		}

		@ParameterizedTest
		@ValueSource(strings = {"USD", "EUR", "GBP", "JPY"})
		@DisplayName("should validate trades in all supported currencies")
		void shouldValidateAllCurrencies(String currency) {
			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					currency,
					TOMORROW
			);

			ValidatedTrade result = validator.apply(trade);

			assertThat(result.currency()).isEqualTo(currency);
		}

		@ParameterizedTest
		@CsvSource({
				"Alice,Bob,1000,USD",
				"Goldman Sachs,JPMorgan,5000000,EUR",
				"Citibank,Bank of America,250000,GBP",
				"HSBC,Barclays,500000,JPY",
				"Morgan Stanley,Credit Suisse,1234567.89,USD"
		})
		@DisplayName("should validate diverse trade scenarios")
		void shouldValidateDiverseScenarios(String party, String counterparty, String amount, String currency) {
			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					party,
					counterparty,
					new BigDecimal(amount),
					currency,
					TOMORROW
			);

			ValidatedTrade result = validator.apply(trade);

			assertThat(result)
					.hasFieldOrPropertyWithValue("party", party)
					.hasFieldOrPropertyWithValue("counterparty", counterparty)
					.hasFieldOrPropertyWithValue("currency", currency);
		}

		@ParameterizedTest
		@CsvSource({
				"100,100",
				"1000,1000",
				"1000000,1000000",
				"50000000.50,50000000.50",
				"0.01,0.01"
		})
		@DisplayName("should preserve various trade amounts")
		void shouldPreserveTradeAmounts(String amountStr, String expectedStr) {
			BigDecimal amount = new BigDecimal(amountStr);
			BigDecimal expected = new BigDecimal(expectedStr);
			IncomingTrade trade = new IncomingTrade(
					UUID.randomUUID().toString(),
					"Alice",
					"Bob",
					amount,
					"USD",
					TOMORROW
			);

			ValidatedTrade result = validator.apply(trade);

			assertThat(result.amount()).isEqualTo(expected);
		}
	}
}
