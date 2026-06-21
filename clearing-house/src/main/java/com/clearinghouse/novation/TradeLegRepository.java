package com.clearinghouse.novation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface TradeLegRepository extends JpaRepository<TradeLegEntity, String> {

    List<TradeLegEntity> findByOriginalTradeId(String originalTradeId);
}
