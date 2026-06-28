package com.clearinghouse.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

class EmailNotifierTest {

    EmailNotifier notifier = new EmailNotifier();

    @Test
    void shouldGenerateConfirmationEmail() {
		EnrichedConfirmation tradeConfirmation = new EnrichedConfirmation(
			"TRD-001",
			"Goldman Sachs",
			"CH-42",
			BigDecimal.valueOf(1_500_000),
			"USD",
			LocalDate.of(2026, 7, 1),
			"US Dollar",
			"New York"
		);

		String html = notifier.apply(tradeConfirmation);

        assertThat(html)
			.contains("<html><body>")
			.contains("<h1>Trade Confirmation</h1>")
			.contains("Trade ID: TRD-001")
            .contains("Counterparty: Goldman Sachs")
            .contains("Amount: 1500000 USD (US Dollar)")
            .contains("Settlement Date: 2026-07-01")
            .contains("New York")
			.contains("</body></html>");
    }

    @Test
    void shouldGenerateConfirmationEmail_localFile() throws IOException {
        EnrichedConfirmation tradeConfirmation = new EnrichedConfirmation(
            "TRD-001",
            "Goldman Sachs",
            "CH-42",
            BigDecimal.valueOf(1_500_000),
            "USD",
            LocalDate.of(2026, 7, 1),
            "US Dollar",
            "New York"
        );

        String actualHtml = notifier.apply(tradeConfirmation);
        String expectedHtml = readFile("notifications/email.html");

        assertThat(actualHtml).isEqualTo(expectedHtml);
    }

	private String readFile(String file) throws IOException {
		return new String(getClass().getClassLoader()
				.getResourceAsStream(file).readAllBytes(),
				StandardCharsets.UTF_8);
	}
}
