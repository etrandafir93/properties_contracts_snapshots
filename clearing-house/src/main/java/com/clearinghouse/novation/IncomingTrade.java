package com.clearinghouse.novation;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomingTrade(
    String tradeId,
    String party,
    String counterparty,
    BigDecimal amount,
    String currency,
    LocalDate settlementDate
) {}
