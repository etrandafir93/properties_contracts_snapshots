package com.clearinghouse.novation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.clearinghouse.validation.ValidatedTrade;

@Tag("unit")
@DisplayName("TradeNovation unit tests")
class TradeNovation_UnitTest {

	private final TradeNovation novation = new TradeNovation();

	@Test
	@DisplayName("should create two trade legs from one validated trade")
	void shouldSplitTradeIntoTwoLegs() {
		ValidatedTrade trade = new ValidatedTrade(
				"TRADE-001",
				"Alice",
				"Bob",
				BigDecimal.valueOf(1_000_000),
				"USD",
				LocalDate.of(2030, 1, 15)
		);

		List<NovatedTrade> legs = novation.apply(trade);

		assertThat(legs).hasSize(2);
	}

	@Test
	@DisplayName("should create one BUY leg for the party")
	void shouldCreateBuyLegForParty() {
		ValidatedTrade trade = new ValidatedTrade(
				"TRADE-001",
				"Goldman Sachs",
				"JPMorgan",
				BigDecimal.valueOf(5_000_000),
				"USD",
				LocalDate.of(2030, 1, 15)
		);

		List<NovatedTrade> legs = novation.apply(trade);

		NovatedTrade buyLeg = legs.stream()
				.filter(leg -> "BUY".equals(leg.side()))
				.findFirst()
				.orElseThrow();

		assertThat(buyLeg)
				.hasFieldOrPropertyWithValue("counterparty", "Goldman Sachs")
				.hasFieldOrPropertyWithValue("side", "BUY")
				.hasFieldOrPropertyWithValue("originalTradeId", "TRADE-001")
				.hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(5_000_000))
				.hasFieldOrPropertyWithValue("currency", "USD")
				.hasFieldOrPropertyWithValue("settlementDate", LocalDate.of(2030, 1, 15));
	}

	@Test
	@DisplayName("should create one SELL leg for the counterparty")
	void shouldCreateSellLegForCounterparty() {
		ValidatedTrade trade = new ValidatedTrade(
				"TRADE-001",
				"Goldman Sachs",
				"JPMorgan",
				BigDecimal.valueOf(5_000_000),
				"USD",
				LocalDate.of(2030, 1, 15)
		);

		List<NovatedTrade> legs = novation.apply(trade);

		NovatedTrade sellLeg = legs.stream()
				.filter(leg -> "SELL".equals(leg.side()))
				.findFirst()
				.orElseThrow();

		assertThat(sellLeg)
				.hasFieldOrPropertyWithValue("counterparty", "JPMorgan")
				.hasFieldOrPropertyWithValue("side", "SELL")
				.hasFieldOrPropertyWithValue("originalTradeId", "TRADE-001")
				.hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(5_000_000))
				.hasFieldOrPropertyWithValue("currency", "USD")
				.hasFieldOrPropertyWithValue("settlementDate", LocalDate.of(2030, 1, 15));
	}

	@Test
	@DisplayName("should assign clearing house ID to both legs")
	void shouldAssignClearingHouseId() {
		ValidatedTrade trade = new ValidatedTrade(
				"TRADE-001",
				"Alice",
				"Bob",
				BigDecimal.valueOf(1000),
				"USD",
				LocalDate.of(2030, 1, 15)
		);

		List<NovatedTrade> legs = novation.apply(trade);

		assertThat(legs)
				.allMatch(leg -> "CH-001".equals(leg.clearingHouseId()));
	}

	@Test
	@DisplayName("should generate unique trade IDs for each leg")
	void shouldGenerateUniqueTradeIds() {
		ValidatedTrade trade = new ValidatedTrade(
				"TRADE-001",
				"Alice",
				"Bob",
				BigDecimal.valueOf(1000),
				"USD",
				LocalDate.of(2030, 1, 15)
		);

		List<NovatedTrade> legs = novation.apply(trade);

		String buyLegId = legs.stream()
				.filter(leg -> "BUY".equals(leg.side()))
				.map(NovatedTrade::tradeId)
				.findFirst()
				.orElseThrow();

		String sellLegId = legs.stream()
				.filter(leg -> "SELL".equals(leg.side()))
				.map(NovatedTrade::tradeId)
				.findFirst()
				.orElseThrow();

		assertThat(buyLegId)
				.isNotEqualTo(sellLegId)
				.isNotBlank();
		assertThat(sellLegId).isNotBlank();
	}

	@Test
	@DisplayName("should generate valid UUIDs for trade IDs")
	void shouldGenerateValidUuids() {
		ValidatedTrade trade = new ValidatedTrade(
				"TRADE-001",
				"Alice",
				"Bob",
				BigDecimal.valueOf(1000),
				"USD",
				LocalDate.of(2030, 1, 15)
		);

		List<NovatedTrade> legs = novation.apply(trade);

		assertThat(legs)
				.allMatch(leg -> {
					try {
						UUID.fromString(leg.tradeId());
						return true;
					} catch (IllegalArgumentException e) {
						return false;
					}
				});
	}

	@ParameterizedTest
	@ValueSource(strings = {"USD", "EUR", "GBP", "JPY"})
	@DisplayName("should preserve currency for all legs")
	void shouldPreserveCurrency(String currency) {
		ValidatedTrade trade = new ValidatedTrade(
				"TRADE-001",
				"Alice",
				"Bob",
				BigDecimal.valueOf(1000),
				currency,
				LocalDate.of(2030, 1, 15)
		);

		List<NovatedTrade> legs = novation.apply(trade);

		assertThat(legs)
				.allMatch(leg -> currency.equals(leg.currency()));
	}

	@ParameterizedTest
	@CsvSource({
			"100,100",
			"1000000,1000000",
			"0.01,0.01"
	})
	@DisplayName("should preserve amount for all legs")
	void shouldPreserveAmount(BigDecimal amount, BigDecimal expectedAmount) {
		ValidatedTrade trade = new ValidatedTrade(
				"TRADE-001",
				"Alice",
				"Bob",
				amount,
				"USD",
				LocalDate.of(2030, 1, 15)
		);

		List<NovatedTrade> legs = novation.apply(trade);

		assertThat(legs)
				.allMatch(leg -> leg.amount().equals(expectedAmount));
	}

	@ParameterizedTest
	@CsvSource({
			"2030-01-15,2030-01-15",
			"2030-06-30,2030-06-30",
			"2031-12-31,2031-12-31"
	})
	@DisplayName("should preserve settlement date for all legs")
	void shouldPreserveSettlementDate(String settlementStr, String expectedStr) {
		LocalDate settlement = LocalDate.parse(settlementStr);
		LocalDate expected = LocalDate.parse(expectedStr);

		ValidatedTrade trade = new ValidatedTrade(
				"TRADE-001",
				"Alice",
				"Bob",
				BigDecimal.valueOf(1000),
				"USD",
				settlement
		);

		List<NovatedTrade> legs = novation.apply(trade);

		assertThat(legs)
				.allMatch(leg -> leg.settlementDate().equals(expected));
	}

	@Test
	@DisplayName("should preserve original trade ID in both legs")
	void shouldPreserveOriginalTradeId() {
		ValidatedTrade trade = new ValidatedTrade(
				"ORIGINAL-TRD-ABC123",
				"Alice",
				"Bob",
				BigDecimal.valueOf(1000),
				"USD",
				LocalDate.of(2030, 1, 15)
		);

		List<NovatedTrade> legs = novation.apply(trade);

		assertThat(legs)
				.allMatch(leg -> "ORIGINAL-TRD-ABC123".equals(leg.originalTradeId()));
	}

	@Test
	@DisplayName("should handle complex trade attributes correctly")
	void shouldHandleComplexTrade() {
		ValidatedTrade trade = new ValidatedTrade(
				"COMPLEX-001",
				"Goldman Sachs",
				"JPMorgan Chase",
				BigDecimal.valueOf(500_000_000.50),
				"EUR",
				LocalDate.of(2030, 12, 25)
		);

		List<NovatedTrade> legs = novation.apply(trade);

		assertThat(legs).hasSize(2)
				.anySatisfy(leg -> assertThat(leg)
						.hasFieldOrPropertyWithValue("counterparty", "Goldman Sachs")
						.hasFieldOrPropertyWithValue("side", "BUY")
						.hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(500_000_000.50))
						.hasFieldOrPropertyWithValue("currency", "EUR")
						.hasFieldOrPropertyWithValue("settlementDate", LocalDate.of(2030, 12, 25)))
				.anySatisfy(leg -> assertThat(leg)
						.hasFieldOrPropertyWithValue("counterparty", "JPMorgan Chase")
						.hasFieldOrPropertyWithValue("side", "SELL"));
	}

	@Test
	@DisplayName("should create idempotent legs with same trade input")
	void shouldCreateConsistentLegsStructure() {
		ValidatedTrade trade = new ValidatedTrade(
				"TRADE-001",
				"Alice",
				"Bob",
				BigDecimal.valueOf(1000),
				"USD",
				LocalDate.of(2030, 1, 15)
		);

		List<NovatedTrade> legs1 = novation.apply(trade);
		List<NovatedTrade> legs2 = novation.apply(trade);

		assertThat(legs1).hasSize(2);
		assertThat(legs2).hasSize(2);

		// Verify structure is consistent (except for UUIDs)
		assertThat(legs1.get(0).side()).isEqualTo(legs2.get(0).side());
		assertThat(legs1.get(1).side()).isEqualTo(legs2.get(1).side());
		assertThat(legs1.get(0).counterparty()).isEqualTo(legs2.get(0).counterparty());
		assertThat(legs1.get(1).counterparty()).isEqualTo(legs2.get(1).counterparty());
	}

	@Nested
	@DisplayName("Multi-parameter novation scenarios")
	class MultiParameterScenarios {

		@ParameterizedTest
		@CsvSource({
				"TRADE-001,Alice,Bob,1000,USD,2030-01-15",
				"TRADE-002,Goldman Sachs,JPMorgan,5000000,EUR,2030-06-30",
				"TRADE-003,Citibank,Bank of America,250000,GBP,2030-03-15",
				"TRADE-004,HSBC,Barclays,3000000,JPY,2030-12-31",
				"TRADE-005,Morgan Stanley,Credit Suisse,750000,USD,2030-09-15"
		})
		@DisplayName("should novate diverse trade scenarios")
		void shouldNovateDiverseScenarios(String tradeId, String party, String counterparty,
				String amount, String currency, String settlementDate) {
			ValidatedTrade trade = new ValidatedTrade(
					tradeId,
					party,
					counterparty,
					new BigDecimal(amount),
					currency,
					LocalDate.parse(settlementDate)
			);

			List<NovatedTrade> legs = novation.apply(trade);

			assertThat(legs).hasSize(2)
					.anySatisfy(leg -> assertThat(leg)
							.hasFieldOrPropertyWithValue("counterparty", party)
							.hasFieldOrPropertyWithValue("side", "BUY"))
					.anySatisfy(leg -> assertThat(leg)
							.hasFieldOrPropertyWithValue("counterparty", counterparty)
							.hasFieldOrPropertyWithValue("side", "SELL"));
		}

		@ParameterizedTest
		@CsvSource({
				"100,100",
				"1000,1000",
				"1000000,1000000",
				"50000000.50,50000000.50",
				"0.01,0.01",
				"123456.789,123456.789",
				"999999999.99,999999999.99"
		})
		@DisplayName("should preserve various amounts in novation")
		void shouldPreserveVariousAmounts(String amountStr, String expectedStr) {
			BigDecimal amount = new BigDecimal(amountStr);
			BigDecimal expected = new BigDecimal(expectedStr);
			ValidatedTrade trade = new ValidatedTrade(
					"TRADE-001",
					"Alice",
					"Bob",
					amount,
					"USD",
					LocalDate.of(2030, 1, 15)
			);

			List<NovatedTrade> legs = novation.apply(trade);

			assertThat(legs)
					.allMatch(leg -> leg.amount().equals(expected));
		}

		@ParameterizedTest
		@CsvSource({
				"2030-01-01",
				"2030-06-15",
				"2030-12-31",
				"2031-03-20",
				"2029-02-28",
				"2032-07-04"
		})
		@DisplayName("should preserve various settlement dates")
		void shouldPreserveVariousSettlementDates(String dateStr) {
			LocalDate settlementDate = LocalDate.parse(dateStr);
			ValidatedTrade trade = new ValidatedTrade(
					"TRADE-001",
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					"USD",
					settlementDate
			);

			List<NovatedTrade> legs = novation.apply(trade);

			assertThat(legs)
					.allMatch(leg -> leg.settlementDate().equals(settlementDate));
		}

		@ParameterizedTest
		@CsvSource({
				"USD",
				"EUR",
				"GBP",
				"JPY",
				"CHF",
				"CAD",
				"AUD",
				"NZD",
				"SGD",
				"HKD"
		})
		@DisplayName("should preserve various currencies")
		void shouldPreserveVariousCurrencies(String currency) {
			ValidatedTrade trade = new ValidatedTrade(
					"TRADE-001",
					"Alice",
					"Bob",
					BigDecimal.valueOf(1000),
					currency,
					LocalDate.of(2030, 1, 15)
			);

			List<NovatedTrade> legs = novation.apply(trade);

			assertThat(legs)
					.allMatch(leg -> leg.currency().equals(currency));
		}

		@ParameterizedTest
		@CsvSource({
				"Bank A,Bank B",
				"Goldman Sachs,JPMorgan Chase",
				"Citibank,Bank of America",
				"Wells Fargo,Morgan Stanley",
				"Bank of New York,Barclays"
		})
		@DisplayName("should correctly assign counterparties to legs")
		void shouldCorrectlyAssignCounterparties(String party, String counterparty) {
			ValidatedTrade trade = new ValidatedTrade(
					"TRADE-001",
					party,
					counterparty,
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15)
			);

			List<NovatedTrade> legs = novation.apply(trade);

			assertThat(legs)
					.filteredOn(leg -> "BUY".equals(leg.side()))
					.allMatch(leg -> party.equals(leg.counterparty()));

			assertThat(legs)
					.filteredOn(leg -> "SELL".equals(leg.side()))
					.allMatch(leg -> counterparty.equals(leg.counterparty()));
		}
	}
}
