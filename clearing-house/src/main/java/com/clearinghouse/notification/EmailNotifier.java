package com.clearinghouse.notification;

import com.clearinghouse.application.Filter;
import com.clearinghouse.application.LogUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Filter("notification-email")
@Slf4j
class EmailNotifier implements Function<EnrichedConfirmation, String> {

    @Override
    public String apply(EnrichedConfirmation c) {
        log.info("{}[notification-email] Generating confirmation email for trade: {} to counterparty: {}{}",
            LogUtils.VIOLET, c.tradeId(), c.counterparty(), LogUtils.RESET);

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

        log.info("{}[notification-email] Confirmation email generated for trade: {}{}", LogUtils.VIOLET, c.tradeId(), LogUtils.RESET);
        return html;
    }
}
