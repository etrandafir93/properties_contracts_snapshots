package com.clearinghouse.novation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface TradeRepository extends JpaRepository<TradeEntity, String> {

    List<TradeEntity> findByOriginalTradeId(String originalTradeId);
}
