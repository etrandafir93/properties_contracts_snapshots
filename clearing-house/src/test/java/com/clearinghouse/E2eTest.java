package com.clearinghouse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.clearinghouse.email.EnrichedConfirmation;
import com.clearinghouse.novation.IncomingTrade;
import com.clearinghouse.novation.NovatedTrade;
import com.clearinghouse.novation.ValidatedTrade;
import com.clearinghouse.persistence.TradeRepository;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@EnableTestBinder
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class E2eTest {

	@Autowired
	private InputDestination input;

	@Autowired
	private OutputDestination output;

	@Autowired
	private TradeRepository tradeRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeAll
	void trigger() {
		var trade = new IncomingTrade("trade-123", "Alice", "Bob",
				BigDecimal.valueOf(1_000), "USD", LocalDate.of(2030, 1, 1));

		input.send(MessageBuilder.withPayload(trade).build(), "incoming-trades");
	}

	@Test
	@Order(1)
	void validate() throws Exception {
		ValidatedTrade validated = receiveOne(output, "validated-trades",
				ValidatedTrade.class);

		assertThat(validated).isNotNull()
				.hasFieldOrPropertyWithValue("tradeId", "trade-123")
				.hasFieldOrPropertyWithValue("counterpartyA", "Alice")
				.hasFieldOrPropertyWithValue("counterpartyB", "Bob")
				.hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(1_000))
				.hasFieldOrPropertyWithValue("currency", "USD")
				.hasFieldOrPropertyWithValue("settlementDate", LocalDate.of(2030, 1, 1));
	}

	@Test
	@Order(2)
	void novate() throws Exception {
		List<NovatedTrade> novatedTrades = receiveOne(output, "novated-trades", new TypeReference<>() {});

		assertThat(novatedTrades)
				.allSatisfy(leg -> assertThat(leg)
						.hasFieldOrPropertyWithValue("originalTradeId", "trade-123")
						.hasFieldOrPropertyWithValue("clearingHouseId", "CH-001")
						.hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(1_000))
						.hasFieldOrPropertyWithValue("currency", "USD"))
				.extracting(NovatedTrade::counterparty)
				.containsExactlyInAnyOrder("Alice", "Bob");
	}

	@Test
	@Order(3)
	void persist() {
		await().untilAsserted(() ->
			assertThat(tradeRepository.findAll())
				.hasSize(2)
				.allMatch(it -> it.getOriginalTradeId().equals("trade-123"))
				.allMatch(it -> it.getAmount().equals(BigDecimal.valueOf(1_000)))
				.allMatch(it -> it.getCurrency().equals("USD"))
				.anyMatch(it -> it.getCounterparty().equals("Alice"))
				.anyMatch(it -> it.getCounterparty().equals("Bob")));
	}

	@Test
	@Order(4)
	void enrich() {
		List<EnrichedConfirmation> confirmations = receiveMany(2, output,
				"novated-trade-confirmations", EnrichedConfirmation.class);

		assertThat(confirmations)
				.allSatisfy(c -> assertThat(c)
						.hasFieldOrPropertyWithValue("currencyName","US Dollar")
						.hasFieldOrPropertyWithValue("settlementLocation", "New York")
						.hasFieldOrPropertyWithValue("clearingHouseId", "CH-001"))
				.extracting(EnrichedConfirmation::counterparty)
				.containsExactlyInAnyOrder("Alice", "Bob");
	}

	@Test
	@Order(5)
	void email() {
		List<String> emails = pollForMessages(2, output, "email-notifications");

		assertThat(emails)
			.anyMatch(it ->
				it.contains("Counterparty: Alice") &&
				it.contains("Amount: 1000 USD (US Dollar)") &&
				it.contains("Settlement Date: 2030-01-01 — New York"))
			.anyMatch(it ->
				it.contains("Counterparty: Bob") &&
				it.contains("Amount: 1000 USD (US Dollar)") &&
				it.contains("Settlement Date: 2030-01-01 — New York"));
	}

	private <T> T receiveOne(OutputDestination output, String channel, Class<T> type)
			throws Exception {
		Message<byte[]> message = output.receive(2_000, channel);
		assertThat(message).as("expected a message on %s", channel).isNotNull();
		return objectMapper.readValue(message.getPayload(), type);
	}

	private <T> T receiveOne(OutputDestination output, String channel,
			TypeReference<T> type) throws Exception {
		Message<byte[]> message = output.receive(2_000, channel);
		assertThat(message).as("expected a message on %s", channel).isNotNull();
		return objectMapper.readValue(message.getPayload(), type);
	}

	private <T> List<T> receiveMany(int count, OutputDestination output, String channel,
			Class<T> type) {
		return IntStream.range(0, count).mapToObj(__ -> output.receive(2_000, channel))
				.filter(Objects::nonNull).map(it -> {
					try {
						return objectMapper.readValue(it.getPayload(), type);
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}).toList();
	}

	private List<String> pollForMessages(int count, OutputDestination output,
			String channel) {
		return IntStream.range(0, count).mapToObj(__ -> output.receive(2_000, channel))
				.filter(Objects::nonNull).map(it -> new String(it.getPayload())).toList();
	}
}
