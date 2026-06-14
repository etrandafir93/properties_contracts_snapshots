package com.clearinghouse.filters;

import com.clearinghouse.domain.NovatedTrade;
import com.clearinghouse.domain.ValidatedTrade;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Component
public class TradeNovation {

    @Bean
    public Function<ValidatedTrade, List<NovatedTrade>> novate() {
        return validatedTrade -> {
            String clearingHouseId = "CH-001";
            String leg1Id = UUID.randomUUID().toString();
            String leg2Id = UUID.randomUUID().toString();

            NovatedTrade leg1 = new NovatedTrade(
                leg1Id,
                validatedTrade.counterpartyA(),
                clearingHouseId,
                validatedTrade.amount(),
                validatedTrade.currency(),
                validatedTrade.settlementDate(),
                validatedTrade.tradeId()
            );

            NovatedTrade leg2 = new NovatedTrade(
                leg2Id,
                validatedTrade.counterpartyB(),
                clearingHouseId,
                validatedTrade.amount(),
                validatedTrade.currency(),
                validatedTrade.settlementDate(),
                validatedTrade.tradeId()
            );

            return List.of(leg1, leg2);
        };
    }
}
