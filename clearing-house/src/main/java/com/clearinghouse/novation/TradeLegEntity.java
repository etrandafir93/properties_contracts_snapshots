package com.clearinghouse.novation;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.validator.constraints.Length;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trades")
@Getter
@NoArgsConstructor
@AllArgsConstructor
class TradeLegEntity {

    @Id
    String tradeId;
	@Length(min = 3, max = 20)
    String counterparty;
    String clearingHouseId;
    @Column(precision = 19)
    BigDecimal amount;
    String currency;
    LocalDate settlementDate;
    String originalTradeId;
    String side;

    static TradeLegEntity from(NovatedTrade trade) {
        return new TradeLegEntity(
            trade.tradeId(),
            trade.counterparty(),
            trade.clearingHouseId(),
            trade.amount(),
            trade.currency(),
            trade.settlementDate(),
            trade.originalTradeId(),
            trade.side()
        );
    }

}
