package com.clearinghouse.email;

import com.clearinghouse.email.EnrichedConfirmation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class EmailNotifier {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotifier.class);

    @Bean
    public Consumer<List<EnrichedConfirmation>> email() {
        return confirmations -> {
            confirmations.forEach(confirmation -> {
                logger.info("Sending confirmation email for trade: {} to counterparty: {}",
                    confirmation.tradeId(), confirmation.counterparty());
            });
        };
    }
}
