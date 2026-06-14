package com.clearinghouse.filters;

import com.clearinghouse.domain.EnrichedConfirmation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class TradeConfirmationPublisher {

    private static final Logger logger = LoggerFactory.getLogger(TradeConfirmationPublisher.class);

    @Bean
    public Consumer<List<EnrichedConfirmation>> publish() {
        return confirmations -> {
            confirmations.forEach(confirmation -> {
                logger.info("Publishing confirmation for trade: {} to counterparty: {}",
                    confirmation.tradeId(), confirmation.counterparty());
            });
        };
    }
}
