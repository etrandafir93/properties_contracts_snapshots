package com.clearinghouse.filters;

import com.clearinghouse.domain.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class ComposedFunctions {

    private final RiskValidator riskValidator;
    private final TradeNovation tradeNovation;
    private final TradeConfirmationEnricher tradeConfirmationEnricher;
    private final TradeConfirmationPublisher tradeConfirmationPublisher;

    public ComposedFunctions(RiskValidator riskValidator,
                            TradeNovation tradeNovation,
                            TradeConfirmationEnricher tradeConfirmationEnricher,
                            TradeConfirmationPublisher tradeConfirmationPublisher) {
        this.riskValidator = riskValidator;
        this.tradeNovation = tradeNovation;
        this.tradeConfirmationEnricher = tradeConfirmationEnricher;
        this.tradeConfirmationPublisher = tradeConfirmationPublisher;
    }

    // Composition 1: validate | novate = riskAndNovate
    @Bean
    public Function<IncomingTrade, List<NovatedTrade>> riskAndNovate() {
        var validateFn = riskValidator.validate();
        var novateFn = tradeNovation.novate();
        return validateFn.andThen(novateFn);
    }

    // Composition 2: enrich | publish = enrichAndPublish
    @Bean
    public Function<List<NovatedTrade>, Void> enrichAndPublish() {
        var enrichFn = tradeConfirmationEnricher.enrich();
        var publishFn = tradeConfirmationPublisher.publish();

        return novatedTrades -> {
            List<EnrichedConfirmation> enriched = enrichFn.apply(novatedTrades);
            publishFn.accept(enriched);
            return null;
        };
    }
}
