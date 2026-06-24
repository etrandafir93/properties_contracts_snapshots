package com.clearinghouse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.clearinghouse.novation.NovatedTrade;
import com.clearinghouse.validation.IncomingTrade;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjectMother {

	static LocalDate VALID_SETTLEMENT = LocalDate.of(2030, 1, 1);

	public static IncomingTrade aTrade() {
        return new IncomingTrade(
				UUID.randomUUID().toString(),
                "Alice",
                "Bob",
                BigDecimal.valueOf(1_000),
                "USD",
                VALID_SETTLEMENT);
    }

	public static NovatedTrade aNovatedTrade() {
		return new NovatedTrade(
				UUID.randomUUID().toString(),
				"Alice",
				"CH-001",
				BigDecimal.valueOf(1_000),
				"USD",
				VALID_SETTLEMENT,
				UUID.randomUUID().toString(),
				"BUY");
	}

}
