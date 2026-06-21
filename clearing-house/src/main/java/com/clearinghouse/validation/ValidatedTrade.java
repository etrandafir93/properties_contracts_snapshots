package com.clearinghouse.validation;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ValidatedTrade(
    String tradeId,
    String party,
    String counterparty,
    BigDecimal amount,
    String currency,
    LocalDate settlementDate
) {}
