package com.clearinghouse.persistence;

import com.clearinghouse.novation.NovatedTrade;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "trades")
public class TradeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tradeId;

    @Column(nullable = false)
    private String counterparty;

    @Column(nullable = false)
    private String clearingHouseId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private LocalDate settlementDate;

    @Column(nullable = false)
    private String originalTradeId;

    public TradeEntity() {}

    public TradeEntity(String tradeId, String counterparty, String clearingHouseId,
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

    public static TradeEntity from(NovatedTrade trade) {
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

    public NovatedTrade toNovatedTrade() {
        return new NovatedTrade(tradeId, counterparty, clearingHouseId, amount, currency, settlementDate, originalTradeId);
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTradeId() { return tradeId; }
    public void setTradeId(String tradeId) { this.tradeId = tradeId; }

    public String getCounterparty() { return counterparty; }
    public void setCounterparty(String counterparty) { this.counterparty = counterparty; }

    public String getClearingHouseId() { return clearingHouseId; }
    public void setClearingHouseId(String clearingHouseId) { this.clearingHouseId = clearingHouseId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDate getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }

    public String getOriginalTradeId() { return originalTradeId; }
    public void setOriginalTradeId(String originalTradeId) { this.originalTradeId = originalTradeId; }
}
