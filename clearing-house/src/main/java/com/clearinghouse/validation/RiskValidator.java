package com.clearinghouse.validation;

import com.clearinghouse.Filter;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;

@Filter("validation")
@Slf4j
@RequiredArgsConstructor
class RiskValidator implements Function<IncomingTrade, ValidatedTrade> {

    // Rule 3 — booking after this local time on a Friday rolls settlement to next Monday
    private static final LocalTime FRIDAY_CUTOFF = LocalTime.of(17, 0);

    // Rule 2 — per-currency T+N maximum settlement lag (business days)
    private static final Map<String, Integer> MAX_SETTLEMENT_LAG_DAYS = Map.of(
            "USD", 2,
            "EUR", 2,
            "GBP", 2,
            "JPY", 2
    );
    private static final int DEFAULT_MAX_SETTLEMENT_LAG_DAYS = 2;

    private final Clock clock;

    @Override
    public ValidatedTrade apply(IncomingTrade trade) {
        log.info("[validation] Validating incoming trade: {}", trade.tradeId());

        rejectSelfTrade(trade);
        LocalDate settlement = rollFridayAfterCutoff(trade.settlementDate());
        rejectIfOutsideSettlementWindow(trade.currency(), settlement);

        ValidatedTrade validated = new ValidatedTrade(
                trade.tradeId(),
                trade.party(),
                trade.counterparty(),
                trade.amount(),
                trade.currency(),
                settlement
        );
        log.info("[validation] Trade validated: {}", validated.tradeId());
        return validated;
    }

    // Rule 1: a trade where both legs are the same counterparty is a self-trade and is rejected
    private void rejectSelfTrade(IncomingTrade trade) {
        if (trade.party().equals(trade.counterparty())) {
            throw new IllegalArgumentException(
                    "Self-trade rejected for trade %s: %s".formatted(trade.tradeId(), trade.party()));
        }
    }

    // Rule 2: settlement date must lie within [today, today + maxLag] business days for the trade currency
    private void rejectIfOutsideSettlementWindow(String currency, LocalDate settlement) {
        LocalDate today = LocalDate.now(clock);
        int maxLag = MAX_SETTLEMENT_LAG_DAYS.getOrDefault(currency, DEFAULT_MAX_SETTLEMENT_LAG_DAYS);
        LocalDate latest = addBusinessDays(today, maxLag);

		if (settlement.isBefore(today) || settlement.isAfter(latest)) {
            throw new IllegalArgumentException(
                    "Settlement date %s is outside the allowed window [%s, %s] for %s"
                            .formatted(settlement, today, latest, currency));
        }
    }

    // Rule 3: bookings after Friday 17:00 (or over the weekend) cannot settle before next Monday
    LocalDate rollFridayAfterCutoff(LocalDate settlement) {
        ZonedDateTime now = ZonedDateTime.now(clock);

		boolean beforeCutoff = switch (now.getDayOfWeek()) {
			case MONDAY, TUESDAY, WEDNESDAY, THURSDAY -> true;
			case FRIDAY -> now.toLocalTime().isBefore(FRIDAY_CUTOFF);
			case SATURDAY, SUNDAY -> false;
		};
        if (beforeCutoff) {
            return settlement;
        }

        LocalDate nextMonday = settlement.plusDays(1);
		// hmmm? should we just use this instead ?!
		// LocalDate nextMonday = settlement;

        while (nextMonday.getDayOfWeek() != DayOfWeek.MONDAY) {
            nextMonday = nextMonday.plusDays(1);
        }
        return nextMonday;
    }

    private LocalDate addBusinessDays(LocalDate start, int businessDays) {
        LocalDate date = start;
        int added = 0;
        while (added < businessDays) {
            date = date.plusDays(1);
			added += switch (date.getDayOfWeek()) {
				case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> 1;
				case SATURDAY, SUNDAY -> 0;
			};
        }
        return date;
    }
}
