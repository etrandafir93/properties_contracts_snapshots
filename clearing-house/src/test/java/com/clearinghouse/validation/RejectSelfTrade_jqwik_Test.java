package com.clearinghouse.validation;

import static com.clearinghouse.ObjectMother.aTrade;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.AlphaChars;

class RejectSelfTrade_jqwik_Test {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2030-01-01T10:00:00Z"), ZoneOffset.UTC);

    private final RiskValidator validator = new RiskValidator(CLOCK);

    @Property
    void shouldRejectWhenPartiesAreEqual(
            @ForAll @AlphaChars String name
    ) {
        IncomingTrade trade = aTrade()
                .withParty(name)
                .withCounterparty(name);

        assertThatThrownBy(() -> validator.apply(trade))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Self-trade rejected");
    }

    @Property
    void shouldNotRejectWhenPartiesDiffer(
            @ForAll @AlphaChars String party,
            @ForAll @AlphaChars String counterparty
    ) {
        assumeThat(party).isNotEqualToIgnoringCase(counterparty);

        IncomingTrade trade = aTrade()
                .withParty(party)
                .withCounterparty(counterparty);

        assertThatCode(() -> validator.apply(trade))
                .as("non-self-trade must not be rejected")
                .doesNotThrowAnyException();
    }
}
