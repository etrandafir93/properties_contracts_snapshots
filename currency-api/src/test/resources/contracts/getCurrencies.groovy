package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Currency API returns all currencies sorted by code"

    request {
        method GET()
        url "/api/currencies"
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            [
                currency_code     : "AUD",
                currency_name     : "Australian Dollar",
                currency_symbol   : "\$",
                settlement_location: "Sydney",
                decimal_places    : 2,
                iso_numeric_code  : 36,
                introduced_on     : "1966-02-14"
            ],
            [
                currency_code     : "CAD",
                currency_name     : "Canadian Dollar",
                currency_symbol   : "\$",
                settlement_location: "Toronto",
                decimal_places    : 2,
                iso_numeric_code  : 124,
                introduced_on     : "1858-01-01"
            ],
            [
                currency_code     : "CHF",
                currency_name     : "Swiss Franc",
                currency_symbol   : "Fr",
                settlement_location: "Zurich",
                decimal_places    : 2,
                iso_numeric_code  : 756,
                introduced_on     : "1850-05-07"
            ],
            [
                currency_code     : "EUR",
                currency_name     : "Euro",
                currency_symbol   : "€",
                settlement_location: "Frankfurt",
                decimal_places    : 2,
                iso_numeric_code  : 978,
                introduced_on     : "1999-01-01"
            ],
            [
                currency_code     : "GBP",
                currency_name     : "British Pound",
                currency_symbol   : "£",
                settlement_location: "London",
                decimal_places    : 2,
                iso_numeric_code  : 826,
                introduced_on     : "1971-02-15"
            ],
            [
                currency_code     : "JPY",
                currency_name     : "Japanese Yen",
                currency_symbol   : "¥",
                settlement_location: "Tokyo",
                decimal_places    : 0,
                iso_numeric_code  : 392,
                introduced_on     : "1871-06-27"
            ],
            [
                currency_code     : "USD",
                currency_name     : "US Dollar",
                currency_symbol   : "\$",
                settlement_location: "New York",
                decimal_places    : 2,
                iso_numeric_code  : 840,
                introduced_on     : "1792-04-02"
            ]
        ])
    }
}
