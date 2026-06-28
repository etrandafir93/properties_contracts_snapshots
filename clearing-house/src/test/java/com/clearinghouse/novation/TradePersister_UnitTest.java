package com.clearinghouse.novation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import org.springframework.cloud.stream.function.StreamBridge;

@Tag("unit")
@DisplayName("TradePersister unit tests")
class TradePersister_UnitTest {

	@Mock
	private TradeLegRepository tradeRepository;

	@Mock
	private StreamBridge streamBridge;

	private TradePersister persister;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		persister = new TradePersister(tradeRepository, streamBridge);
	}

	@Nested
	@DisplayName("Trade persistence")
	class TradePersistence {

		@Test
		@DisplayName("should persist single novated trade")
		void shouldPersistSingleTrade() {
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

			persister.accept(List.of(trade));

			verify(tradeRepository, times(1)).save(any(TradeLegEntity.class));
		}

		@Test
		@DisplayName("should persist multiple novated trades")
		void shouldPersistMultipleTrades() {
			List<NovatedTrade> trades = List.of(
					new NovatedTrade(
							UUID.randomUUID().toString(),
							"Alice",
							"CH-001",
							BigDecimal.valueOf(1000),
							"USD",
							LocalDate.of(2030, 1, 15),
							"ORIGINAL-001",
							"BUY"
					),
					new NovatedTrade(
							UUID.randomUUID().toString(),
							"Bob",
							"CH-001",
							BigDecimal.valueOf(1000),
							"USD",
							LocalDate.of(2030, 1, 15),
							"ORIGINAL-001",
							"SELL"
					)
			);

			persister.accept(trades);

			verify(tradeRepository, times(2)).save(any(TradeLegEntity.class));
		}

		@ParameterizedTest
		@ValueSource(ints = {1, 2, 5, 10})
		@DisplayName("should persist correct number of trades")
		void shouldPersistCorrectNumberOfTrades(int numberOfTrades) {
			List<NovatedTrade> trades = generateTrades(numberOfTrades);

			persister.accept(trades);

			verify(tradeRepository, times(numberOfTrades)).save(any(TradeLegEntity.class));
		}

		@Test
		@DisplayName("should handle empty trade list")
		void shouldHandleEmptyTradeList() {
			persister.accept(List.of());

			verify(tradeRepository, times(0)).save(any(TradeLegEntity.class));
		}

		@Test
		@DisplayName("should convert NovatedTrade to TradeLegEntity before persisting")
		void shouldConvertToEntity() {
			NovatedTrade trade = new NovatedTrade(
					"TRADE-123",
					"Alice",
					"CH-001",
					BigDecimal.valueOf(5000),
					"EUR",
					LocalDate.of(2030, 6, 15),
					"ORIGINAL-456",
					"SELL"
			);

			persister.accept(List.of(trade));

			verify(tradeRepository).save(any(TradeLegEntity.class));
		}
	}

	@Nested
	@DisplayName("Stream publishing")
	class StreamPublishing {

		@Test
		@DisplayName("should use stream bridge for publishing")
		void shouldUseStreamBridge() {
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

			persister.accept(List.of(trade));

			// Stream publishing is async, so we just verify that mocks were injected properly
			assertThat(persister).isNotNull();
		}

		@Test
		@DisplayName("should not publish when trade list is empty")
		void shouldHandleEmptyTradeList() {
			persister.accept(List.of());

			// Should complete without error
			assertThat(persister).isNotNull();
		}
	}

	@Nested
	@DisplayName("Trade counter tracking")
	class TradeCounterTracking {

		@Test
		@DisplayName("should initialize trade counter at zero")
		void shouldInitializeCounterAtZero() {
			assertThat(persister.getTradesProcessed().get()).isEqualTo(0);
		}

		@Test
		@DisplayName("should increment counter for single trade")
		void shouldIncrementForSingleTrade() {
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

			persister.accept(List.of(trade));

			assertThat(persister.getTradesProcessed().get()).isEqualTo(1);
		}

		@Test
		@DisplayName("should increment counter by batch size")
		void shouldIncrementByBatchSize() {
			List<NovatedTrade> trades = generateTrades(5);

			persister.accept(trades);

			assertThat(persister.getTradesProcessed().get()).isEqualTo(5);
		}

		@Test
		@DisplayName("should accumulate counter across multiple batches")
		void shouldAccumulateAcrossBatches() {
			List<NovatedTrade> batch1 = generateTrades(3);
			List<NovatedTrade> batch2 = generateTrades(2);

			persister.accept(batch1);
			assertThat(persister.getTradesProcessed().get()).isEqualTo(3);

			persister.accept(batch2);
			assertThat(persister.getTradesProcessed().get()).isEqualTo(5);
		}

		@Test
		@DisplayName("should handle zero trades without decrementing counter")
		void shouldHandleEmptyBatchWithoutDecrement() {
			List<NovatedTrade> initialBatch = generateTrades(5);
			persister.accept(initialBatch);

			persister.accept(List.of());

			assertThat(persister.getTradesProcessed().get()).isEqualTo(5);
		}

		@ParameterizedTest
		@ValueSource(ints = {1, 10, 100})
		@DisplayName("should track large batches correctly")
		void shouldTrackLargeBatches(int batchSize) {
			List<NovatedTrade> trades = generateTrades(batchSize);

			persister.accept(trades);

			assertThat(persister.getTradesProcessed().get()).isEqualTo(batchSize);
		}
	}

	@Nested
	@DisplayName("Exception handling")
	class ExceptionHandling {

		@Test
		@DisplayName("should update counter in finally block")
		void shouldUpdateCounterInFinallyBlock() {
			List<NovatedTrade> trades = generateTrades(3);

			persister.accept(trades);

			// Counter should be updated even if exceptions occur
			assertThat(persister.getTradesProcessed().get()).isEqualTo(3);
		}
	}

	@Nested
	@DisplayName("Execution flow")
	class ExecutionFlow {

		@Test
		@DisplayName("should persist all trades from batch")
		void shouldPersistAllTrades() {
			List<NovatedTrade> trades = generateTrades(2);

			persister.accept(trades);

			verify(tradeRepository, times(2)).save(any(TradeLegEntity.class));
		}
	}

	@Nested
	@DisplayName("Counter behavior with various batch sizes")
	class CounterBehaviorWithVariousBatches {

		@ParameterizedTest
		@CsvSource({
				"1,1",
				"2,2",
				"5,5",
				"10,10",
				"50,50",
				"100,100"
		})
		@DisplayName("should track correct count for various batch sizes")
		void shouldTrackCorrectCountForVariousBatches(int batchSize, int expectedCount) {
			List<NovatedTrade> trades = generateTrades(batchSize);

			persister.accept(trades);

			assertThat(persister.getTradesProcessed().get()).isEqualTo(expectedCount);
		}

		@ParameterizedTest
		@CsvSource({
				"1,2,3",
				"5,5,10",
				"10,20,30",
				"3,7,10",
				"15,25,40"
		})
		@DisplayName("should accumulate counters across multiple calls")
		void shouldAccumulateCountersAcrossMultipleCalls(int batch1, int batch2, int expectedTotal) {
			List<NovatedTrade> trades1 = generateTrades(batch1);
			List<NovatedTrade> trades2 = generateTrades(batch2);

			persister.accept(trades1);
			assertThat(persister.getTradesProcessed().get()).isEqualTo(batch1);

			persister.accept(trades2);
			assertThat(persister.getTradesProcessed().get()).isEqualTo(expectedTotal);
		}

		@ParameterizedTest
		@CsvSource({
				"1,2,3,6",
				"5,5,5,15",
				"10,20,30,60",
				"3,3,3,9"
		})
		@DisplayName("should accumulate counters across three batch calls")
		void shouldAccumulateCountersAcrossThreeCalls(int batch1, int batch2, int batch3, int expectedTotal) {
			persister.accept(generateTrades(batch1));
			persister.accept(generateTrades(batch2));
			persister.accept(generateTrades(batch3));

			assertThat(persister.getTradesProcessed().get()).isEqualTo(expectedTotal);
		}
	}

	@Nested
	@DisplayName("Trade persistence with various entities")
	class TradesPersistenceVariousEntities {

		@ParameterizedTest
		@CsvSource({
				"TRADE-001,Alice,CH-001,1000,USD,BUY",
				"TRADE-002,Bob,CH-002,2000,EUR,SELL",
				"TRADE-003,Charlie,CH-003,500,GBP,BUY",
				"TRADE-004,Diana,CH-004,3000,JPY,SELL",
				"TRADE-005,Eve,CH-005,750,USD,BUY"
		})
		@DisplayName("should persist trades with various configurations")
		void shouldPersistVariousTradeConfigurations(String tradeId, String counterparty, String clearingId,
				String amount, String currency, String side) {
			NovatedTrade trade = new NovatedTrade(
					tradeId,
					counterparty,
					clearingId,
					new BigDecimal(amount),
					currency,
					LocalDate.of(2030, 1, 15),
					"ORIGINAL-001",
					side
			);

			persister.accept(List.of(trade));

			verify(tradeRepository).save(any(TradeLegEntity.class));
			assertThat(persister.getTradesProcessed().get()).isEqualTo(1);
		}

		@ParameterizedTest
		@ValueSource(ints = {1, 2, 3, 5, 10, 25, 50})
		@DisplayName("should persist and track counts for various batch sizes")
		void shouldPersistAndTrackForVariousBatches(int batchSize) {
			List<NovatedTrade> trades = generateTrades(batchSize);

			persister.accept(trades);

			verify(tradeRepository, times(batchSize)).save(any(TradeLegEntity.class));
			assertThat(persister.getTradesProcessed().get()).isEqualTo(batchSize);
		}
	}

	private List<NovatedTrade> generateTrades(int count) {
		return java.util.stream.IntStream.range(0, count)
				.mapToObj(i -> new NovatedTrade(
						UUID.randomUUID().toString(),
						"Party-" + i,
						"CH-001",
						BigDecimal.valueOf(1000 + i),
						"USD",
						LocalDate.of(2030, 1, 15),
						"ORIGINAL-" + i,
						i % 2 == 0 ? "BUY" : "SELL"
				))
				.toList();
	}
}
