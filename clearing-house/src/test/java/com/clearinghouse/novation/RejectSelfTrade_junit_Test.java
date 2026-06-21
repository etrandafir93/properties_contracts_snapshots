package com.clearinghouse.novation;

import static com.clearinghouse.ObjectMother.aTrade;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;

class RejectSelfTrade_junit_Test {

    // Tuesday 2030-01-01 10:00 UTC — keeps the Friday cutoff dormant
    // and lets the trade settle same-day inside the T+2 window.
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2030-01-01T10:00:00Z"), ZoneOffset.UTC);

    private static final Random RANDOM = new Random(42);

    private final RiskValidator validator = new RiskValidator(CLOCK);

    @RepeatedTest(200)
    void shouldRejectWhenPartiesAreEqual() {
        String name = randomString(8);

        IncomingTrade trade = aTrade()
                .withParty(name)
                .withCounterparty(name);

        assertThatThrownBy(() -> validator.apply(trade))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Self-trade rejected");
    }

    @RepeatedTest(200)
    void shouldNotRejectWhenPartiesDiffer() {
        String party = randomString(8);
        String counterparty = randomString(8);
        assumeThat(party).isNotEqualToIgnoringCase(counterparty);

        IncomingTrade trade = aTrade()
                .withParty(party)
                .withCounterparty(counterparty);

        assertThatCode(() -> validator.apply(trade))
                .as("non-self-trade must not be rejected")
                .doesNotThrowAnyException();
    }

    private static String randomString(int size) {
        return RANDOM
                .ints('a', 'z')
                .mapToObj(ch -> String.valueOf((char) ch))
                .limit(size)
                .collect(joining());
    }
}
