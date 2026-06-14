package com.clearinghouse.email;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EnrichedConfirmation(
    String tradeId,
    String counterparty,
    String clearingHouseId,
    BigDecimal amount,
    String currency,
    LocalDate settlementDate,
    String currencyName,
    String settlementLocation
) {}
