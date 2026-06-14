package com.clearinghouse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.clearinghouse.email.EnrichedConfirmation;
import com.clearinghouse.novation.IncomingTrade;
import com.clearinghouse.novation.NovatedTrade;
import com.clearinghouse.novation.ValidatedTrade;
import com.clearinghouse.persistence.TradeRepository;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TradeClearingE2ETest {

    @MockitoSpyBean StreamBridge streamBridge;

    @Autowired @Qualifier("validate") Function<IncomingTrade, ValidatedTrade> validate;
    @Autowired @Qualifier("novate") Function<ValidatedTrade, List<NovatedTrade>> novate;
    @Autowired @Qualifier("persist") Consumer<List<NovatedTrade>> persist;
    @Autowired @Qualifier("enrich") Function<NovatedTrade, EnrichedConfirmation> enrich;
    @Autowired @Qualifier("email") Function<EnrichedConfirmation, String> email;
    @Autowired TradeRepository tradeRepository;

    private static final IncomingTrade INCOMING_TRADE = new IncomingTrade(
        "TRADE-001", "CPTY-A", "CPTY-B",
        new BigDecimal("1000000.00"), "USD", LocalDate.of(2025, 12, 31)
    );

    private ValidatedTrade validatedTrade;
    private List<NovatedTrade> novatedLegs;
    private EnrichedConfirmation enrichedConfirmation;
    private String emailHtml;

    @Test @Order(1)
    void validate() {
        validatedTrade = validate.apply(INCOMING_TRADE);

        assertThat(validatedTrade.tradeId()).isEqualTo("TRADE-001");
        assertThat(validatedTrade.counterpartyA()).isEqualTo("CPTY-A");
        assertThat(validatedTrade.counterpartyB()).isEqualTo("CPTY-B");
        assertThat(validatedTrade.amount()).isEqualByComparingTo("1000000.00");
        assertThat(validatedTrade.currency()).isEqualTo("USD");
    }

    @Test @Order(2)
    void novate() {
        novatedLegs = novate.apply(validatedTrade);

        assertThat(novatedLegs).hasSize(2);
        assertThat(novatedLegs).extracting(NovatedTrade::counterparty)
            .containsExactlyInAnyOrder("CPTY-A", "CPTY-B");
        assertThat(novatedLegs).allSatisfy(leg -> {
            assertThat(leg.clearingHouseId()).isEqualTo("CH-001");
            assertThat(leg.originalTradeId()).isEqualTo("TRADE-001");
        });
    }

    @Test @Order(3)
    void persist() {
        persist.accept(novatedLegs);

        verify(streamBridge, times(2))
				.send(eq("novated-trades"), any(NovatedTrade.class));
    }

    @Test @Order(4)
    void enrich() {
        enrichedConfirmation = enrich.apply(novatedLegs.get(0));

        assertThat(enrichedConfirmation.tradeId()).isEqualTo(novatedLegs.get(0).tradeId());
        assertThat(enrichedConfirmation.currencyName()).isEqualTo("US Dollar");
        assertThat(enrichedConfirmation.settlementLocation()).isEqualTo("New York");
    }

    @Test @Order(5)
    void email() {
        emailHtml = email.apply(enrichedConfirmation);

        assertThat(emailHtml)
            .contains("<html>")
            .contains("Trade Confirmation")
            .contains(enrichedConfirmation.tradeId())
            .contains("CPTY-A")
            .contains("US Dollar")
            .contains("New York");
    }
}
