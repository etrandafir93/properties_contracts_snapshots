package com.clearinghouse;

import com.fasterxml.jackson.annotation.JsonProperty;

record Currency(
    @JsonProperty("currency_code") String code,
    @JsonProperty("currency_name") String name,
    @JsonProperty("currency_symbol") String symbol,
    @JsonProperty("settlement_location") String settlementLocation,
    @JsonProperty("decimal_places") int decimalPlaces,
    @JsonProperty("introduced_on") String introducedOn,
	@Deprecated(forRemoval = true)
	@JsonProperty("iso_numeric_code") int isoNumericCode
) {}
