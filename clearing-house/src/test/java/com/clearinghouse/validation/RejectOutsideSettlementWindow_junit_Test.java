package com.clearinghouse.validation;

import static com.clearinghouse.ObjectMother.aTrade;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class RejectOutsideSettlementWindow_junit_Test {

    // Tuesday 2030-01-01 10:00 UTC — keeps the Friday cutoff dormant.
    // With USD (T+2 business days) the latest accepted settlement is 2030-01-03.
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2030-01-01T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 1);
    private static final LocalDate LATEST_VALID = LocalDate.of(2030, 1, 3);

    private static final Random RANDOM = new Random(42);

    private final RiskValidator validator = new RiskValidator(CLOCK);

    @RepeatedTest(200)
    void shouldRejectWhenSettlementIsPastTheWindow() {
        int daysPastWindow = RANDOM.nextInt(1, 365);

        LocalDate settlementDate = LATEST_VALID.plusDays(daysPastWindow);

        IncomingTrade trade = aTrade()
                .withSettlementDate(settlementDate);

        assertThatThrownBy(() -> validator.apply(trade))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside the allowed window");
    }

    @RepeatedTest(200)
    void shouldAcceptWhenSettlementIsWithinTheWindow() {
        int daysFromToday = RANDOM.nextInt(0, 3);

        LocalDate settlementDate = TODAY.plusDays(daysFromToday);

        IncomingTrade trade = aTrade()
                .withSettlementDate(settlementDate);

        assertThatCode(() -> validator.apply(trade))
                .doesNotThrowAnyException();
    }

    @Test
    void latestValidSettlementIsAccepted() {
        IncomingTrade trade = aTrade()
				.withSettlementDate(LATEST_VALID);

        assertThatCode(() -> validator.apply(trade))
                .doesNotThrowAnyException();
    }

    @Test
    void dayAfterLatestValidIsRejected() {
        IncomingTrade trade = aTrade()
				.withSettlementDate(LATEST_VALID.plusDays(1));

        assertThatThrownBy(() -> validator.apply(trade))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside the allowed window");
    }
}
