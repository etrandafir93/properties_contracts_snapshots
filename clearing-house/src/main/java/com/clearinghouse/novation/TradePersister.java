package com.clearinghouse.novation;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.cloud.stream.function.StreamBridge;

import com.clearinghouse.application.Filter;
import com.clearinghouse.application.LogUtils;
import com.clearinghouse.novation.NovatedTrade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Filter("novation-persist")
class TradePersister implements Consumer<List<NovatedTrade>> {

    final TradeRepository tradeRepository;
    final StreamBridge streamBridge;

    @Override
    public void accept(List<NovatedTrade> trades) {
        log.info("{}[novation-persist] Persisting {} trades{}", LogUtils.BLUE, trades.size(), LogUtils.RESET);
        trades.stream()
				.map(TradeEntity::from)
				.forEach(tradeRepository::save);

		trades.stream()
				.peek(trade -> log.info("{}[novation-persist] Trade persisted: {}{}", LogUtils.BLUE, trade.tradeId(), LogUtils.RESET))
				.forEach(trade -> supplyAsync(() ->
					streamBridge.send("persisted-trades", trade)));
    }
}
