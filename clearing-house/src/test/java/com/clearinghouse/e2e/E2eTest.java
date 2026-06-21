package com.clearinghouse.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import com.clearinghouse.notification.EnrichedConfirmation;
import com.clearinghouse.validation.IncomingTrade;
import com.clearinghouse.novation.NovatedTrade;
import com.clearinghouse.validation.ValidatedTrade;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@EnableTestBinder
@TestMethodOrder(OrderAnnotation.class)
@EnableWireMock(@ConfigureWireMock(
		name = "currency-api",
		baseUrlProperties = "currency-api.url"
))
@Import(FixedClockConfig.class)
class E2eTest {

	@Autowired
	private InputDestination input;

	@Autowired
	private OutputDestination output;

	@Autowired
	private JdbcClient jdbcClient;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@Order(1)
	@DisplayName("validation")
	void validate() {
		var trade = new IncomingTrade("trade-123", "Alice", "Bob",
				BigDecimal.valueOf(1_000), "USD", LocalDate.of(2030, 1, 1));
		input.send(MessageBuilder.withPayload(trade).build(), "incoming-trades");

		ValidatedTrade validated = receiveOne(output, "validated-trades",
				ValidatedTrade.class);

		assertThat(validated).isNotNull()
				.hasFieldOrPropertyWithValue("tradeId", "trade-123")
				.hasFieldOrPropertyWithValue("party", "Alice")
				.hasFieldOrPropertyWithValue("counterparty", "Bob")
				.hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(1_000))
				.hasFieldOrPropertyWithValue("currency", "USD")
				.hasFieldOrPropertyWithValue("settlementDate", LocalDate.of(2030, 1, 1));
	}

	@Order(2)
	@DisplayName("novation - split")
	@ParameterizedTest(name = "counterparty {0}")
	@ValueSource(strings = {"Alice", "Bob"})
	void novate(String counterparty) {
		List<NovatedTrade> novatedTrades = receiveOne(output, "novated-trades", new TypeReference<>() {});

		assertThat(novatedTrades)
				.anyMatch(leg ->
						leg.counterparty().equals(counterparty) &&
						leg.originalTradeId().equals("trade-123") &&
						leg.clearingHouseId().equals("CH-001") &&
						leg.amount().equals(BigDecimal.valueOf(1_000)) &&
						leg.currency().equals("USD"));
	}

	@Order(3)
	@DisplayName("novation - persist")
	@ParameterizedTest(name = "counterparty {0}")
	@ValueSource(strings = {"Alice", "Bob"})
	void persist(String counterparty) {
		await().untilAsserted(() -> {
			List<Map<String, Object>> rows = jdbcClient
					.sql("SELECT counterparty, original_trade_id, amount, currency FROM trades")
					.query()
					.listOfRows();

			assertThat(rows)
					.hasSize(2)
					.anyMatch(row ->
						row.get("counterparty").equals(counterparty)
							&& row.get("original_trade_id").equals("trade-123")
							&& row.get("amount").equals(BigDecimal.valueOf(1_000))
							&& row.get("currency").equals("USD"));
		});
	}

	@Order(4)
	@DisplayName("notification - enrich")
	@ParameterizedTest(name = "counterparty {0}")
	@ValueSource(strings = {"Alice", "Bob"})
	void enrich(String counterparty) {
		List<EnrichedConfirmation> confirmations = receiveMany(2, output,
				"novated-trade-confirmations", EnrichedConfirmation.class);

		assertThat(confirmations)
				.anyMatch(c ->
						c.counterparty().equals(counterparty) &&
						c.currencyName().equals("US Dollar (USD)") &&
						c.settlementLocation().equals("New York") &&
						c.clearingHouseId().equals("CH-001"));
	}

	@Order(5)
	@DisplayName("notification - email")
	@ParameterizedTest(name = "counterparty {0}")
	@ValueSource(strings = {"Alice", "Bob"})
	void email(String counterparty) {
		List<String> emails = receiveMany(2, output, "email-notifications", String::new);

		assertThat(emails)
			.anyMatch(it ->
				it.contains("Counterparty: " + counterparty) &&
				it.contains("Amount: 1000 USD (US Dollar (USD))") &&
				it.contains("Settlement Date: 2030-01-01 — New York"));
	}

	private <T> T receiveOne(OutputDestination output, String channel, Class<T> type) {
		return receiveMany(1, output, channel, type).stream().findFirst().orElseThrow();
	}

	private <T> T receiveOne(OutputDestination output, String channel, TypeReference<T> type) {
		return receiveMany(1, output, channel, payload -> objectMapper.readValue(payload, type))
				.stream().findFirst().orElseThrow();
	}

	private <T> List<T> receiveMany(int count, OutputDestination output, String channel,
			Class<T> type) {
		return receiveMany(count, output, channel, payload -> objectMapper.readValue(payload, type));
	}

	private <T> List<T> receiveMany(int count, OutputDestination output, String channel,
			java.util.function.Function<byte[], T> mapper) {
		if (receivedMessages.containsKey(channel)) {
			return (List<T>) receivedMessages.get(channel);
		}

		var msgs = IntStream.range(0, count)
				.mapToObj(__ -> output.receive(2_000, channel))
				.filter(Objects::nonNull)
				.map(Message::getPayload)
				.map(mapper)
				.toList();

		receivedMessages.put(channel, msgs);
		return msgs;
	}

	private static Map<String, List<?>> receivedMessages = new HashMap<>();
}
