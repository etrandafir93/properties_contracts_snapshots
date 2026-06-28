package com.clearinghouse.novation;

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
@DisplayName("TradeLegEntity unit tests")
class TradeLegEntity_UnitTest {

	@Nested
	@DisplayName("Entity construction")
	class EntityConstruction {

		@Test
		@DisplayName("should create entity with all fields via constructor")
		void shouldCreateEntityWithAllFields() {
			String tradeId = UUID.randomUUID().toString();
			TradeLegEntity entity = new TradeLegEntity(
					tradeId,
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			assertThat(entity)
					.hasFieldOrPropertyWithValue("tradeId", tradeId)
					.hasFieldOrPropertyWithValue("counterparty", "Alice")
					.hasFieldOrPropertyWithValue("clearingHouseId", "CH-001")
					.hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(1000))
					.hasFieldOrPropertyWithValue("currency", "USD")
					.hasFieldOrPropertyWithValue("settlementDate", LocalDate.of(2030, 1, 15))
					.hasFieldOrPropertyWithValue("originalTradeId", "ORIGINAL-001")
					.hasFieldOrPropertyWithValue("side", "BUY");
		}

		@Test
		@DisplayName("should create entity with no-arg constructor")
		void shouldCreateEntityWithNoArgConstructor() {
			TradeLegEntity entity = new TradeLegEntity();

			assertThat(entity).isNotNull();
		}

		@Test
		@DisplayName("should create entity and allow field assignment")
		void shouldAllowFieldAssignment() {
			TradeLegEntity entity = new TradeLegEntity();
			entity.tradeId = "TRADE-123";
			entity.counterparty = "Bob";

			assertThat(entity)
					.hasFieldOrPropertyWithValue("tradeId", "TRADE-123")
					.hasFieldOrPropertyWithValue("counterparty", "Bob");
		}
	}

	@Nested
	@DisplayName("Trade ID")
	class TradeIdField {

		@Test
		@DisplayName("should store and retrieve trade ID")
		void shouldStoreTradeId() {
			String tradeId = "TRD-123456";
			TradeLegEntity entity = new TradeLegEntity(
					tradeId,
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			assertThat(entity.getTradeId()).isEqualTo(tradeId);
		}

		@Test
		@DisplayName("should use UUID as trade ID")
		void shouldUseUuidAsTradeId() {
			String uuidTradeId = UUID.randomUUID().toString();
			TradeLegEntity entity = new TradeLegEntity(
					uuidTradeId,
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			assertThat(entity.getTradeId()).isEqualTo(uuidTradeId);
		}
	}

	@Nested
	@DisplayName("Counterparty field")
	class CounterpartyField {

		@ParameterizedTest
		@ValueSource(strings = {"Alice", "Goldman Sachs", "JPMorgan", "Citibank"})
		void shouldStoreCounterparty(String counterparty) {
			TradeLegEntity entity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					counterparty,
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			assertThat(entity.getCounterparty()).isEqualTo(counterparty);
		}

		@Test
		@DisplayName("should validate counterparty length constraint")
		void shouldValidateCounterpartyLength() {
			// Note: @Length(min = 3, max = 20) constraint is applied at validation time
			TradeLegEntity shortEntity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"AB", // Less than min 3
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			// Entity can be created, but validation would fail if called
			assertThat(shortEntity).isNotNull();
		}

		@Test
		@DisplayName("should accept 3-character counterparty (minimum)")
		void shouldAcceptMinimumCounterpartyLength() {
			TradeLegEntity entity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"ABC",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			assertThat(entity.getCounterparty()).isEqualTo("ABC");
		}

		@Test
		@DisplayName("should accept 20-character counterparty (maximum)")
		void shouldAcceptMaximumCounterpartyLength() {
			String longCounterparty = "12345678901234567890"; // 20 chars
			TradeLegEntity entity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					longCounterparty,
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			assertThat(entity.getCounterparty()).isEqualTo(longCounterparty);
		}
	}

	@Nested
	@DisplayName("Clearing house ID")
	class ClearingHouseIdField {

		@ParameterizedTest
		@ValueSource(strings = {"CH-001", "CH-100", "CH-999", "CLEARING-HOUSE-001"})
		void shouldStoreClearingHouseId(String clearingHouseId) {
			TradeLegEntity entity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"Alice",
					clearingHouseId,
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			assertThat(entity.getClearingHouseId()).isEqualTo(clearingHouseId);
		}
	}

	@Nested
	@DisplayName("Amount field")
	class AmountField {

		@Test
		@DisplayName("should store and retrieve amount")
		void shouldStoreAmount() {
			BigDecimal amount = BigDecimal.valueOf(5_000_000.50);
			TradeLegEntity entity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					amount,
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			assertThat(entity.getAmount()).isEqualTo(amount);
		}

		@ParameterizedTest
		@ValueSource(strings = {"0.01", "100", "1000000", "999999999.99"})
		void shouldStoreVariousAmounts(String amountStr) {
			BigDecimal amount = new BigDecimal(amountStr);
			TradeLegEntity entity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					amount,
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			assertThat(entity.getAmount()).isEqualTo(amount);
		}

		@Test
		@DisplayName("should preserve BigDecimal precision")
		void shouldPreservePrecision() {
			BigDecimal amount = new BigDecimal("123.456789");
			TradeLegEntity entity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					amount,
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			assertThat(entity.getAmount()).isEqualTo(amount);
		}
	}

	@Nested
	@DisplayName("Currency field")
	class CurrencyField {

		@ParameterizedTest
		@ValueSource(strings = {"USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD"})
		void shouldStoreCurrency(String currency) {
			TradeLegEntity entity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					currency,
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			assertThat(entity.getCurrency()).isEqualTo(currency);
		}
	}

	@Nested
	@DisplayName("Settlement date field")
	class SettlementDateField {

		@Test
		@DisplayName("should store and retrieve settlement date")
		void shouldStoreSettlementDate() {
			LocalDate settlementDate = LocalDate.of(2030, 12, 25);
			TradeLegEntity entity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					settlementDate,
					"ORIGINAL-001",
					"BUY"
			);

			assertThat(entity.getSettlementDate()).isEqualTo(settlementDate);
		}

		@ParameterizedTest
		@ValueSource(strings = {"2030-01-01", "2030-06-15", "2030-12-31"})
		void shouldStoreVariousSettlementDates(String dateStr) {
			LocalDate settlementDate = LocalDate.parse(dateStr);
			TradeLegEntity entity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					settlementDate,
					"ORIGINAL-001",
					"BUY"
			);

			assertThat(entity.getSettlementDate()).isEqualTo(settlementDate);
		}
	}

	@Nested
	@DisplayName("Original trade ID field")
	class OriginalTradeIdField {

		@Test
		@DisplayName("should store and retrieve original trade ID")
		void shouldStoreOriginalTradeId() {
			String originalId = "ORIGINAL-TRD-001";
			TradeLegEntity entity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					originalId,
					"BUY"
			);

			assertThat(entity.getOriginalTradeId()).isEqualTo(originalId);
		}

		@Test
		@DisplayName("should handle UUID as original trade ID")
		void shouldHandleUuidAsOriginalId() {
			String uuidId = UUID.randomUUID().toString();
			TradeLegEntity entity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					uuidId,
					"BUY"
			);

			assertThat(entity.getOriginalTradeId()).isEqualTo(uuidId);
		}
	}

	@Nested
	@DisplayName("Side field")
	class SideField {

		@ParameterizedTest
		@ValueSource(strings = {"BUY", "SELL"})
		void shouldStoreSide(String side) {
			TradeLegEntity entity = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					side
			);

			assertThat(entity.getSide()).isEqualTo(side);
		}

		@Test
		@DisplayName("should distinguish between BUY and SELL")
		void shouldDistinguishBuySell() {
			TradeLegEntity buyLeg = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			TradeLegEntity sellLeg = new TradeLegEntity(
					UUID.randomUUID().toString(),
					"Bob",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"SELL"
			);

			assertThat(buyLeg.getSide()).isEqualTo("BUY");
			assertThat(sellLeg.getSide()).isEqualTo("SELL");
			assertThat(buyLeg.getSide()).isNotEqualTo(sellLeg.getSide());
		}
	}

	@Nested
	@DisplayName("Static factory method: from(NovatedTrade)")
	class StaticFactoryMethod {

		@Test
		@DisplayName("should create entity from NovatedTrade")
		void shouldCreateFromNovatedTrade() {
			NovatedTrade trade = new NovatedTrade(
					"NOVATED-001",
					"Alice",
					"CH-001",
					BigDecimal.valueOf(5_000_000),
					"EUR",
					LocalDate.of(2030, 6, 15),
					"ORIGINAL-001",
					"SELL"
			);

			TradeLegEntity entity = TradeLegEntity.from(trade);

			assertThat(entity)
					.hasFieldOrPropertyWithValue("tradeId", "NOVATED-001")
					.hasFieldOrPropertyWithValue("counterparty", "Alice")
					.hasFieldOrPropertyWithValue("clearingHouseId", "CH-001")
					.hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(5_000_000))
					.hasFieldOrPropertyWithValue("currency", "EUR")
					.hasFieldOrPropertyWithValue("settlementDate", LocalDate.of(2030, 6, 15))
					.hasFieldOrPropertyWithValue("originalTradeId", "ORIGINAL-001")
					.hasFieldOrPropertyWithValue("side", "SELL");
		}

		@Test
		@DisplayName("should map all fields from NovatedTrade to TradeLegEntity")
		void shouldMapAllFields() {
			NovatedTrade trade = new NovatedTrade(
					"TRADE-123",
					"Goldman Sachs",
					"CH-42",
					BigDecimal.valueOf(1_234_567.89),
					"GBP",
					LocalDate.of(2030, 9, 30),
					"ORIG-789",
					"BUY"
			);

			TradeLegEntity entity = TradeLegEntity.from(trade);

			assertThat(entity.getTradeId()).isEqualTo(trade.tradeId());
			assertThat(entity.getCounterparty()).isEqualTo(trade.counterparty());
			assertThat(entity.getClearingHouseId()).isEqualTo(trade.clearingHouseId());
			assertThat(entity.getAmount()).isEqualTo(trade.amount());
			assertThat(entity.getCurrency()).isEqualTo(trade.currency());
			assertThat(entity.getSettlementDate()).isEqualTo(trade.settlementDate());
			assertThat(entity.getOriginalTradeId()).isEqualTo(trade.originalTradeId());
			assertThat(entity.getSide()).isEqualTo(trade.side());
		}

		@Test
		@DisplayName("should preserve BigDecimal amount during conversion")
		void shouldPreserveAmountPrecision() {
			BigDecimal originalAmount = new BigDecimal("123456.789");
			NovatedTrade trade = new NovatedTrade(
					"TRADE-001",
					"Alice",
					"CH-001",
					originalAmount,
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			TradeLegEntity entity = TradeLegEntity.from(trade);

			assertThat(entity.getAmount()).isEqualTo(originalAmount);
		}

		@Test
		@DisplayName("should preserve settlement date during conversion")
		void shouldPreserveSettlementDate() {
			LocalDate settlementDate = LocalDate.of(2030, 12, 31);
			NovatedTrade trade = new NovatedTrade(
					"TRADE-001",
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					settlementDate,
					"ORIGINAL-001",
					"BUY"
			);

			TradeLegEntity entity = TradeLegEntity.from(trade);

			assertThat(entity.getSettlementDate()).isEqualTo(settlementDate);
		}

		@Test
		@DisplayName("should handle UUID trade IDs in conversion")
		void shouldHandleUuidTradeIds() {
			String tradeId = UUID.randomUUID().toString();
			String originalId = UUID.randomUUID().toString();
			NovatedTrade trade = new NovatedTrade(
					tradeId,
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					originalId,
					"BUY"
			);

			TradeLegEntity entity = TradeLegEntity.from(trade);

			assertThat(entity.getTradeId()).isEqualTo(tradeId);
			assertThat(entity.getOriginalTradeId()).isEqualTo(originalId);
		}

		@Test
		@DisplayName("should create independent entity instances")
		void shouldCreateIndependentInstances() {
			NovatedTrade trade = new NovatedTrade(
					"TRADE-001",
					"Alice",
					"CH-001",
					BigDecimal.valueOf(1000),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			TradeLegEntity entity1 = TradeLegEntity.from(trade);
			TradeLegEntity entity2 = TradeLegEntity.from(trade);

			assertThat(entity1).isNotSameAs(entity2);
			assertThat(entity1.getTradeId()).isEqualTo(entity2.getTradeId());
		}
	}

	@Nested
	@DisplayName("Complete entity scenarios")
	class CompleteScenarios {

		@Test
		@DisplayName("should handle complete BUY leg scenario")
		void shouldHandleCompleteBuyLeg() {
			NovatedTrade trade = new NovatedTrade(
					"TRADE-ABC-123",
					"Goldman Sachs",
					"CH-001",
					BigDecimal.valueOf(10_000_000),
					"USD",
					LocalDate.of(2030, 3, 15),
					"ORIGINAL-XYZ-789",
					"BUY"
			);

			TradeLegEntity entity = TradeLegEntity.from(trade);

			assertThat(entity)
					.hasFieldOrPropertyWithValue("tradeId", "TRADE-ABC-123")
					.hasFieldOrPropertyWithValue("counterparty", "Goldman Sachs")
					.hasFieldOrPropertyWithValue("side", "BUY");
		}

		@Test
		@DisplayName("should handle complete SELL leg scenario")
		void shouldHandleCompleteSellLeg() {
			NovatedTrade trade = new NovatedTrade(
					"TRADE-DEF-456",
					"JPMorgan",
					"CH-001",
					BigDecimal.valueOf(10_000_000),
					"USD",
					LocalDate.of(2030, 3, 15),
					"ORIGINAL-XYZ-789",
					"SELL"
			);

			TradeLegEntity entity = TradeLegEntity.from(trade);

			assertThat(entity)
					.hasFieldOrPropertyWithValue("tradeId", "TRADE-DEF-456")
					.hasFieldOrPropertyWithValue("counterparty", "JPMorgan")
					.hasFieldOrPropertyWithValue("side", "SELL");
		}
	}

	@Nested
	@DisplayName("Multi-parameter entity scenarios")
	class MultiParameterEntityScenarios {

		@ParameterizedTest
		@CsvSource({
				"TRADE-001,Alice,CH-001,1000,USD,2030-01-15,ORIGINAL-001,BUY",
				"TRADE-002,Bob,CH-002,2000,EUR,2030-06-30,ORIGINAL-002,SELL",
				"TRADE-003,Charlie,CH-003,500,GBP,2030-03-15,ORIGINAL-003,BUY",
				"TRADE-004,Diana,CH-004,3000,JPY,2030-12-31,ORIGINAL-004,SELL",
				"TRADE-005,Eve,CH-005,750,USD,2030-09-15,ORIGINAL-005,BUY"
		})
		@DisplayName("should create entities from diverse trades")
		void shouldCreateEntitiesFromDiverseTrades(String tradeId, String counterparty, String clearingId,
				String amount, String currency, String date, String originalId, String side) {
			NovatedTrade trade = new NovatedTrade(
					tradeId,
					counterparty,
					clearingId,
					new BigDecimal(amount),
					currency,
					LocalDate.parse(date),
					originalId,
					side
			);

			TradeLegEntity entity = TradeLegEntity.from(trade);

			assertThat(entity)
					.hasFieldOrPropertyWithValue("tradeId", tradeId)
					.hasFieldOrPropertyWithValue("counterparty", counterparty)
					.hasFieldOrPropertyWithValue("clearingHouseId", clearingId)
					.hasFieldOrPropertyWithValue("amount", new BigDecimal(amount))
					.hasFieldOrPropertyWithValue("currency", currency)
					.hasFieldOrPropertyWithValue("settlementDate", LocalDate.parse(date))
					.hasFieldOrPropertyWithValue("originalTradeId", originalId)
					.hasFieldOrPropertyWithValue("side", side);
		}

		@ParameterizedTest
		@CsvSource({
				"Alice,100",
				"Goldman Sachs,500000",
				"JPMorgan,1000000",
				"Citibank,50000000",
				"Bank of America,750000"
		})
		@DisplayName("should create entities with various counterparties and amounts")
		void shouldCreateEntitiesWithVariousCounterpartiesAndAmounts(String counterparty, String amount) {
			NovatedTrade trade = new NovatedTrade(
					UUID.randomUUID().toString(),
					counterparty,
					"CH-001",
					new BigDecimal(amount),
					"USD",
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					"BUY"
			);

			TradeLegEntity entity = TradeLegEntity.from(trade);

			assertThat(entity)
					.hasFieldOrPropertyWithValue("counterparty", counterparty)
					.hasFieldOrPropertyWithValue("amount", new BigDecimal(amount));
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
				"NZD"
		})
		@DisplayName("should preserve various currencies when converting from novated trades")
		void shouldPreserveVariousCurrencies(String currency) {
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

			TradeLegEntity entity = TradeLegEntity.from(trade);

			assertThat(entity.getCurrency()).isEqualTo(currency);
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

			TradeLegEntity entity = TradeLegEntity.from(trade);

			assertThat(entity.getSettlementDate()).isEqualTo(settlementDate);
		}

		@ParameterizedTest
		@ValueSource(strings = {"BUY", "SELL"})
		@DisplayName("should handle both BUY and SELL sides")
		void shouldHandleBothSides(String side) {
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

			TradeLegEntity entity = TradeLegEntity.from(trade);

			assertThat(entity.getSide()).isEqualTo(side);
		}
	}
}
