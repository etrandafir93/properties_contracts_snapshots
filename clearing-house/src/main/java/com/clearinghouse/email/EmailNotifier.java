package com.clearinghouse.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component("email")
public class EmailNotifier implements Consumer<List<EnrichedConfirmation>> {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotifier.class);

    @Override
    public void accept(List<EnrichedConfirmation> confirmations) {
        confirmations.forEach(confirmation -> {
            logger.info("Sending confirmation email for trade: {} to counterparty: {}",
                confirmation.tradeId(), confirmation.counterparty());
        });
    }
}
