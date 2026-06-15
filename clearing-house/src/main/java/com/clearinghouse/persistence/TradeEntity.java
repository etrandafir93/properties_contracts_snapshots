package com.clearinghouse.persistence;

import com.clearinghouse.novation.NovatedTrade;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;

@Getter
public class TradeEntity {

    String tradeId;
    String counterparty;
    String clearingHouseId;
    BigDecimal amount;
    String currency;
    LocalDate settlementDate;
    String originalTradeId;

    TradeEntity(String tradeId, String counterparty, String clearingHouseId,
                BigDecimal amount, String currency, LocalDate settlementDate,
                String originalTradeId) {
        this.tradeId = tradeId;
        this.counterparty = counterparty;
        this.clearingHouseId = clearingHouseId;
        this.amount = amount;
        this.currency = currency;
        this.settlementDate = settlementDate;
        this.originalTradeId = originalTradeId;
    }

    static TradeEntity from(NovatedTrade trade) {
        return new TradeEntity(
            trade.tradeId(),
            trade.counterparty(),
            trade.clearingHouseId(),
            trade.amount(),
            trade.currency(),
            trade.settlementDate(),
            trade.originalTradeId()
        );
    }

    NovatedTrade toNovatedTrade() {
        return new NovatedTrade(tradeId, counterparty, clearingHouseId, amount, currency, settlementDate, originalTradeId);
    }
}
