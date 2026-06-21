package com.clearinghouse.novation;

import com.clearinghouse.novation.NovatedTrade;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trades")
@Getter
@NoArgsConstructor
class TradeEntity {

    @Id
    String tradeId;
    String counterparty;
    String clearingHouseId;
    @Column(precision = 19, scale = 0)
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
