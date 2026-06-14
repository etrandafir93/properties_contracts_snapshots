package com.clearinghouse.domain;

public record Currency(
    String code,
    String name,
    String settlementLocation,
    int decimalPlaces
) {}
