package com.clearinghouse.notification;

import static com.clearinghouse.ObjectMother.aNovatedTrade;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

class TradeConfirmationEnricher_ContractTest {

    @RegisterExtension
    static StubRunnerExtension stubRunner = new StubRunnerExtension()
            .downloadStub("com.clearinghouse", "currency-api")
            .stubsMode(StubRunnerProperties.StubsMode.LOCAL);

    private TradeConfirmationEnricher enricher;

    @BeforeEach
    void setUp() {
        int port = stubRunner.findStubUrl("com.clearinghouse", "currency-api").getPort();
        var client = new CurrencyApiClient("http://localhost:" + port);
        enricher = new TradeConfirmationEnricher(client);
        enricher.loadCurrencies();
    }

    @Test
    void shouldEnrichTrade_usd() {
        var result = enricher.apply(aNovatedTrade().withCurrency("USD"));

        assertThat(result)
                .hasFieldOrPropertyWithValue("currencyName", "US Dollar (USD)")
                .hasFieldOrPropertyWithValue("settlementLocation", "New York");
    }

    @Test
    void shouldEnrichTrade_eur() {
        var result = enricher.apply(aNovatedTrade().withCurrency("EUR"));

        assertThat(result)
                .hasFieldOrPropertyWithValue("currencyName", "Euro (978)")
                .hasFieldOrPropertyWithValue("settlementLocation", "Frankfurt");
    }
}
