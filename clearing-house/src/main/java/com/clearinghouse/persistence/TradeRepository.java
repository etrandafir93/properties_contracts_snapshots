package com.clearinghouse.persistence;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class TradeRepository {

    private final ConcurrentMap<String, TradeEntity> store = new ConcurrentHashMap<>();

    public void save(TradeEntity entity) {
        store.put(entity.tradeId, entity);
    }

    public Optional<TradeEntity> findById(String tradeId) {
        return Optional.ofNullable(store.get(tradeId));
    }

    public Collection<TradeEntity> findAll() {
        return store.values();
    }

    public void deleteAll() {
        store.clear();
    }
}
