package com.clearinghouse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

record CurrencyDto(
    @JsonProperty("currency_code") String code,
    @JsonProperty("currency_name") String name,
    @JsonProperty("currency_symbol") String symbol,
    @JsonProperty("settlement_location") String settlementLocation,
    @JsonProperty("decimal_places") int decimalPlaces,
    @JsonProperty("iso_numeric_code") int isoNumericCode,
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("introduced_on") LocalDate introducedOn
) {
    static CurrencyDto from(CurrencyEntity e) {
        return new CurrencyDto(e.code(), e.name(), e.symbol(), e.settlementLocation(),
                e.decimalPlaces(), e.isoNumericCode(), e.introducedOn());
    }
}
