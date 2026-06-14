package com.clearinghouse.novation;

import com.clearinghouse.Filter;
import com.clearinghouse.LogUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Filter("validate")
@Slf4j
class RiskValidator implements Function<IncomingTrade, ValidatedTrade> {

    @Override
    public ValidatedTrade apply(IncomingTrade incomingTrade) {
        log.info("{}[validate] Validating incoming trade: {}{}", LogUtils.YELLOW, incomingTrade.tradeId(), LogUtils.RESET);
        ValidatedTrade validated = new ValidatedTrade(
            incomingTrade.tradeId(),
            incomingTrade.counterpartyA(),
            incomingTrade.counterpartyB(),
            incomingTrade.amount(),
            incomingTrade.currency(),
            incomingTrade.settlementDate()
        );
        log.info("{}[validate] Trade validated: {}{}", LogUtils.YELLOW, validated.tradeId(), LogUtils.RESET);
        return validated;
    }
}
