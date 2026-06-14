package com.clearinghouse.novation;

import com.clearinghouse.novation.IncomingTrade;
import com.clearinghouse.novation.ValidatedTrade;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class RiskValidator {

    @Bean
    public Function<IncomingTrade, ValidatedTrade> validate() {
        return incomingTrade -> new ValidatedTrade(
            incomingTrade.tradeId(),
            incomingTrade.counterpartyA(),
            incomingTrade.counterpartyB(),
            incomingTrade.amount(),
            incomingTrade.currency(),
            incomingTrade.settlementDate()
        );
    }
}
