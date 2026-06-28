package com.clearinghouse.notification;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDate;

@Tag("snapshot")
@ExtendWith(SnapshotExtension.class)
class EmailNotifier_SnapshotTest {

    private final EmailNotifier notifier = new EmailNotifier();

	private Expect expect;

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

        expect.toMatchSnapshot(html);
    }
}
