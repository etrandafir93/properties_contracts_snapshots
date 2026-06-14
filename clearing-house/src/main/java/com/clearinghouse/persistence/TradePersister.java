package com.clearinghouse.persistence;

import com.clearinghouse.novation.NovatedTrade;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component("persist")
public class TradePersister implements Function<List<NovatedTrade>, List<NovatedTrade>> {

    private final TradeRepository tradeRepository;

    public TradePersister(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Override
    public List<NovatedTrade> apply(List<NovatedTrade> novatedTrades) {
        novatedTrades.forEach(trade -> {
            TradeEntity entity = TradeEntity.from(trade);
            tradeRepository.save(entity);
        });
        return novatedTrades;
    }
}
