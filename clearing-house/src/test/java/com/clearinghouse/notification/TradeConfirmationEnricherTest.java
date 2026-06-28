package com.clearinghouse.notification;

import static com.clearinghouse.ObjectMother.aNovatedTrade;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

@Tag("unit")
class TradeConfirmationEnricherTest {

    @RegisterExtension
    static WireMockExtension currencyApi = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().usingFilesUnderDirectory("src/test/resources"))
            .build();

    private TradeConfirmationEnricher enricher;

    @BeforeEach
    void setUp() {
        var client = new CurrencyApiClient(currencyApi.baseUrl());
        enricher = new TradeConfirmationEnricher(client);
        enricher.loadCurrencies();
    }

    @Test
    void shouldEnrichTrade_usd() {
        var usdTrade = aNovatedTrade()
			.withCurrency("USD");

        var result = enricher.apply(usdTrade);

		// USD, iso 840 (not > 900) → uses currency code
		assertThat(result)
			.hasFieldOrPropertyWithValue("currencyName", "US Dollar (USD)")
			.hasFieldOrPropertyWithValue("settlementLocation", "New York");
    }

    @Test
    void shouldEnrichTrade_eur() {
        var eurTrade = aNovatedTrade()
			.withCurrency("EUR");

        var result = enricher.apply(eurTrade);

        // iso_numeric_code 978 > 900 → uses numeric code string
        assertThat(result)
			.hasFieldOrPropertyWithValue("currencyName", "Euro (978)")
			.hasFieldOrPropertyWithValue("settlementLocation", "Frankfurt");
    }

}
