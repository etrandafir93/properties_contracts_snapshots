package com.clearinghouse.filters;

import com.clearinghouse.domain.NovatedTrade;
import com.clearinghouse.domain.TradeEntity;
import com.clearinghouse.domain.TradeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class NovatedTradeRepository {

    private final TradeRepository tradeRepository;

    public NovatedTradeRepository(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Bean
    public Function<List<NovatedTrade>, List<NovatedTrade>> persist() {
        return novatedTrades -> {
            novatedTrades.forEach(trade -> {
                TradeEntity entity = TradeEntity.from(trade);
                tradeRepository.save(entity);
            });
            return novatedTrades;
        };
    }
}
