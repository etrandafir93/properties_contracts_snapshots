package com.clearinghouse.novation;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NovatedTrade(
    String tradeId,
    String counterparty,
    String clearingHouseId,
    BigDecimal amount,
    String currency,
    LocalDate settlementDate,
    String originalTradeId
) {}
