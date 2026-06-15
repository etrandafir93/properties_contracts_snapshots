package com.clearinghouse.email;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Component
class CurrencyApiClient {

    private final RestClient restClient;

    CurrencyApiClient(@Value("${currency-api.url}") String currencyApiUrl) {
        this.restClient = RestClient.create(currencyApiUrl);
    }

    List<CurrencyDto> fetchAll() {
        return restClient.get()
                .uri("/api/currencies")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    record CurrencyDto(
            @JsonProperty("currency_code") String code,
            @JsonProperty("currency_name") String name,
            @JsonProperty("currency_symbol") String symbol,
            @JsonProperty("settlement_location") String settlementLocation,
            @JsonProperty("decimal_places") int decimalPlaces,
            @JsonProperty("iso_numeric_code") int isoNumericCode,
            @JsonFormat(pattern = "yyyy-MM-dd")
            @JsonProperty("introduced_on") LocalDate introducedOn) {
    }
}
