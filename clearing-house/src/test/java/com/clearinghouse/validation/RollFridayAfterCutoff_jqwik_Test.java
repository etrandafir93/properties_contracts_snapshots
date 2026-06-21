package com.clearinghouse.validation;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.SoftAssertions;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.time.api.constraints.DateRange;
import net.jqwik.time.api.constraints.DateTimeRange;
import net.jqwik.time.api.constraints.DayOfWeekRange;

class RollFridayAfterCutoff_jqwik_Test {

    @Property
    void shouldNotRollSettlementOnMondayThroughThursday(
            @ForAll
			@DateTimeRange(
				min = "2020-01-01T00:00",
				max = "2040-12-31T23:59"
			)
			@DayOfWeekRange(
				min = MONDAY,
				max = THURSDAY
			)
			LocalDateTime clockDate
    ) {
		Clock clock = Clock.fixed(clockDate.toInstant(UTC), UTC);
		RiskValidator validator = new RiskValidator(clock);

		LocalDate settlement = LocalDate.now(clock);

        assertThat(validator.rollFridayAfterCutoff(settlement))
                .isEqualTo(settlement);
    }

	@Property
    void shouldRollSettlementToNextMondayOnWeekends(
			@ForAll
			@DateTimeRange(
					min = "2020-01-01T00:00",
					max = "2040-12-31T23:59"
			)
			@DayOfWeekRange(
				min = SATURDAY,
				max = SUNDAY
			)
			LocalDateTime clockDate
    ) {
		Clock clock = Clock.fixed(clockDate.toInstant(UTC), UTC);
		RiskValidator validator = new RiskValidator(clock);

		LocalDate settlement = LocalDate.now(clock);
        LocalDate nextMonday = nextMonday(clock);

		assertThat(validator.rollFridayAfterCutoff(settlement))
                .isEqualTo(nextMonday);
    }

	@Property
	void shouldBeIdempotent(
			@ForAll
			@DateTimeRange(
				min = "2020-01-01T00:00",
				max = "2040-12-31T23:59"
			)
			LocalDateTime clockDate,
			@ForAll
			@DateRange(
				min = "2020-01-01",
				max = "2040-12-31"
			)
			LocalDate settlement
	) {
		Clock clock = Clock.fixed(clockDate.toInstant(UTC), UTC);
		RiskValidator validator = new RiskValidator(clock);

		LocalDate result1 = validator.rollFridayAfterCutoff(settlement);
		LocalDate result2 = validator.rollFridayAfterCutoff(result1);
		LocalDate result3 = validator.rollFridayAfterCutoff(result2);
		LocalDate result4 = validator.rollFridayAfterCutoff(result3);

		assertThat(List.of(result1, result2, result3, result4))
			.allMatch(result1::equals);
	}

	private static LocalDate nextMonday(Clock clock) {
        LocalDate d = LocalDate.now(clock);
        while (d.getDayOfWeek() != MONDAY) {
            d = d.plusDays(1);
        }
        return d;
    }

	//		System.out.println(LocalDateTime.now(clock)
	//				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd (EEE) HH:mm:ss")));

}
