package com.clearinghouse.novation;

import com.clearinghouse.Filter;

import java.util.function.Function;

@Filter("validate")
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
