package com.clearinghouse;

import java.time.LocalDate;

record CurrencyEntity(
    String code,
    String name,
    String symbol,
    String settlementLocation,
    int decimalPlaces,
    int isoNumericCode,
    LocalDate introducedOn
) {}
