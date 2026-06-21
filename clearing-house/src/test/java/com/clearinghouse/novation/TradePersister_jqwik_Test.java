package com.clearinghouse.novation;

import static java.util.stream.Collectors.groupingBy;
import static net.jqwik.api.Combinators.combine;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;

import com.clearinghouse.e2e.IntegrationTestBase;
import com.clearinghouse.validation.ValidatedTrade;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.arbitraries.StringArbitrary;
import net.jqwik.api.constraints.Size;
import net.jqwik.spring.JqwikSpringSupport;
import net.jqwik.time.api.Dates;
import net.jqwik.time.api.arbitraries.LocalDateArbitrary;

@JqwikSpringSupport
class TradePersister_jqwik_Test extends IntegrationTestBase {

	@Autowired
	TradeLegRepository tradeLegRepository;

	@Autowired
	TradePersister tradeLegPersister;

	@Property(tries = 50)
	void novatedLegsAreBalancedAcrossBothSides(
			@ForAll("trades") @Size(max = 10) List<ValidatedTrade> trades
	) {
		cleanup();
		sendValidatedTradesMessages(trades);
		awaitForProcessedMessages(trades.size() * 2);

		// then
		Map<String, Long> totalsBySide = tradeLegRepository.findAll()
				.stream()
				.collect(groupingBy(
						it -> it.getSide() + "-" + it.getCurrency(),
						Collectors.summingLong(it -> it.getAmount().longValue())
				));

		assertSoftly(softly -> {
			softly.assertThat(totalsBySide.getOrDefault("BUY-USD", 0L))
				.isEqualTo(totalsBySide.getOrDefault("SELL-USD", 0L));

			softly.assertThat(totalsBySide.getOrDefault("BUY-EUR", 0L))
				.isEqualTo(totalsBySide.getOrDefault("SELL-EUR", 0L));

			softly.assertThat(totalsBySide.getOrDefault("BUY-GBP", 0L))
				.isEqualTo(totalsBySide.getOrDefault("SELL-GBP", 0L));

			softly.assertThat(totalsBySide.getOrDefault("BUY-JPY", 0L))
				.isEqualTo(totalsBySide.getOrDefault("SELL-JPY", 0L));
		});
	}

	private void cleanup() {
		tradeLegRepository.deleteAll();
		tradeLegPersister.getTradesProcessed().set(0);
	}

	private void awaitForProcessedMessages(int noOfProcessedMessages) {
		await().untilAsserted(() ->
				assertThat(tradeLegPersister.getTradesProcessed())
						.hasValue(noOfProcessedMessages));
	}

	private void sendValidatedTradesMessages(List<ValidatedTrade> trades) {
		trades.stream()
				.map(trade -> MessageBuilder.withPayload(trade).build())
				.forEach(msg -> input.send(msg, "validated-trades"));
	}

	@Provide
	static Arbitrary<List<ValidatedTrade>> trades() {
		return combine(counterpartyNames(), counterpartyNames(), amounts(), currencies(), settlementDates())
				.as((party, counterparty, amount, currency, settlementDate) ->
						new ValidatedTrade(
								UUID.randomUUID().toString(),
								party, counterparty,
								amount, currency,
								settlementDate
				))
				.list();
	}

	@Provide
	static LocalDateArbitrary settlementDates() {
		return Dates.dates().atTheEarliest(LocalDate.of(2025, 1, 1))
				.atTheLatest(LocalDate.of(2030, 12, 31));
	}

	@Provide
	static Arbitrary<String> currencies() {
		return Arbitraries.of("USD", "EUR", "GBP", "JPY");
	}

	@Provide
	static Arbitrary<BigDecimal> amounts() {
		return Arbitraries.longs()
				.between(1L, 10_000_000L)
				.map(BigDecimal::valueOf);
	}

	@Provide
	static StringArbitrary counterpartyNames() {
		return Arbitraries.strings()
				.alpha()
				.ofMinLength(3)
				.ofMaxLength(20); // valid length
	}
}