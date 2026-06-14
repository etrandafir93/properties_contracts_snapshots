package com.clearinghouse.api;

import com.clearinghouse.domain.IncomingTrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);
    private final StreamBridge streamBridge;

    public TradeController(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @PostMapping
    public void submitTrade(@RequestBody IncomingTrade trade) {
        logger.info("Received trade: {}", trade.tradeId());
        streamBridge.send("validate-out-0", trade);
    }
}
