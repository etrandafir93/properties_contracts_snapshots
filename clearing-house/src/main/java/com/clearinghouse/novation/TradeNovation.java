package com.clearinghouse.novation;

import com.clearinghouse.application.Filter;
import com.clearinghouse.application.LogUtils;
import com.clearinghouse.validation.ValidatedTrade;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Filter("novation-split")
@Slf4j
class TradeNovation implements Function<ValidatedTrade, List<NovatedTrade>> {

    @Override
    public List<NovatedTrade> apply(ValidatedTrade trade) {
        log.info("{}[novation-split] Novating trade: {}{}", LogUtils.GREEN, trade.tradeId(), LogUtils.RESET);
        List<NovatedTrade> legs = List.of(
            leg(trade.party(), trade),
            leg(trade.counterparty(), trade)
        );
        log.info("{}[novation-split] Trade novated into {} legs: {}{}", LogUtils.GREEN, legs.size(), trade.tradeId(), LogUtils.RESET);
        return legs;
    }

    private NovatedTrade leg(String counterparty, ValidatedTrade trade) {
        return new NovatedTrade(
            UUID.randomUUID().toString(),
            counterparty,
            "CH-001",
            trade.amount(),
            trade.currency(),
            trade.settlementDate(),
            trade.tradeId()
        );
    }
}
