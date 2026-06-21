package com.clearinghouse.novation;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.springframework.cloud.stream.function.StreamBridge;

import com.clearinghouse.Filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Filter("novation-persist")
class TradePersister implements Consumer<List<NovatedTrade>> {

    final TradeLegRepository tradeRepository;
    final StreamBridge streamBridge;

	// dummy field for testing/demo purposes
	@Getter
	final AtomicInteger tradesProcessed = new AtomicInteger(0);

    @Override
    public void accept(List<NovatedTrade> trades) {
        log.info("[novation-persist] Persisting {} trades", trades.size());
		try {
			trades.stream()
					.map(TradeLegEntity::from)
					.forEach(tradeRepository::save);

			trades.stream()
					.peek(trade ->
							log.info("[novation-persist] Trade persisted: {}", trade.tradeId()))
					.forEach(trade -> supplyAsync(() ->
						streamBridge.send("persisted-trades", trade)));

		} catch (Exception e) {
			log.error("[novation-persist] Error persisting trades: {} {}",
					trades, e.getMessage(), e);
		} finally {
			tradesProcessed.getAndUpdate(count -> count + trades.size());
		}
    }
}
