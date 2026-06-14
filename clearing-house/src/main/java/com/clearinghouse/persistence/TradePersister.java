package com.clearinghouse.persistence;

import com.clearinghouse.Filter;
import com.clearinghouse.LogUtils;
import com.clearinghouse.novation.NovatedTrade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.List;
import java.util.function.Consumer;

@Filter("persist")
@RequiredArgsConstructor
@Slf4j
class TradePersister implements Consumer<List<NovatedTrade>> {

    final TradeRepository tradeRepository;
    final StreamBridge streamBridge;

    @Override
    public void accept(List<NovatedTrade> trades) {
        log.info("{}[persist] Persisting {} trades{}", LogUtils.BLUE, trades.size(), LogUtils.RESET);
        trades.forEach(trade -> tradeRepository.save(TradeEntity.from(trade)));
        trades.forEach(trade -> streamBridge.send("novated-trades", trade));
        log.info("{}[persist] {} trades persisted and forwarded to novated-trades{}", LogUtils.BLUE, trades.size(), LogUtils.RESET);
    }
}
