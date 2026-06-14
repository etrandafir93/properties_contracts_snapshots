package com.clearinghouse.novation;

import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component("validate")
public class RiskValidator implements Function<IncomingTrade, ValidatedTrade> {

    @Override
    public ValidatedTrade apply(IncomingTrade incomingTrade) {
        return new ValidatedTrade(
            incomingTrade.tradeId(),
            incomingTrade.counterpartyA(),
            incomingTrade.counterpartyB(),
            incomingTrade.amount(),
            incomingTrade.currency(),
            incomingTrade.settlementDate()
        );
    }
}
