package com.clearinghouse.notification;

import com.clearinghouse.Filter;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Filter("notification-email")
@Slf4j
class EmailNotifier implements Function<EnrichedConfirmation, String> {

    @Override
    public String apply(EnrichedConfirmation c) {
        log.info("[notification-email] Generating confirmation email for trade: {} to counterparty: {}", c.tradeId(), c.counterparty());

		String html = """
            <html><body>
              <h1>Trade Confirmation</h1>
              <p>Trade ID: %s</p>
              <p>Counterparty: %s</p>
              <p>Amount: %s %s (%s)</p>
              <p>Settlement Date: %s — %s</p>
            </body></html>
            """.formatted(
                c.tradeId(), c.counterparty(),
                c.amount(), c.currency(), c.currencyName(),
                c.settlementDate(), c.settlementLocation()
            );

        log.info("[notification-email] Confirmation email generated for trade: {}", c.tradeId());
        return html;
    }
}
