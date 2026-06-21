package com.clearinghouse.novation;

import static com.clearinghouse.ObjectMother.aTrade;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

class RejectOutsideSettlementWindow_jqwik_Test {

    // Tuesday 2030-01-01 10:00 UTC — keeps the Friday cutoff dormant.
    // With USD (T+2 business days) the latest accepted settlement is 2030-01-03.
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2030-01-01T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 1);
    private static final LocalDate LATEST_VALID = LocalDate.of(2030, 1, 3);

    private final RiskValidator validator = new RiskValidator(CLOCK);

    @Property
    void shouldRejectWhenSettlementIsPastTheWindow(
            @ForAll @IntRange(min = 1, max = 365) int daysPastWindow
    ) {
        LocalDate settlementDate = LATEST_VALID.plusDays(daysPastWindow);

        IncomingTrade trade = aTrade()
                .withSettlementDate(settlementDate);

        assertThatThrownBy(() -> validator.apply(trade))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside the allowed window");
    }

    @Property
    void shouldAcceptWhenSettlementIsWithinTheWindow(
            @ForAll @IntRange(max = 2) int daysFromToday
    ) {
        LocalDate settlementDate = TODAY.plusDays(daysFromToday);

        IncomingTrade trade = aTrade()
                .withSettlementDate(settlementDate);

        assertThatCode(() -> validator.apply(trade))
                .doesNotThrowAnyException();
    }

    @Example
    void latestValidSettlementIsAccepted() {
        IncomingTrade trade = aTrade()
				.withSettlementDate(LATEST_VALID);

        assertThatCode(() -> validator.apply(trade))
                .doesNotThrowAnyException();
    }

    @Example
    void dayAfterLatestValidIsRejected() {
        IncomingTrade trade = aTrade()
				.withSettlementDate(LATEST_VALID.plusDays(1));

        assertThatThrownBy(() -> validator.apply(trade))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside the allowed window");
    }
}
