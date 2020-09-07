package com.investment.metal.dto;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Builder
public class MetalInfo {

    @Getter
    private final String metalSymbol;

    @Getter
    private final double amountPurchased;

    @Getter
    private final double costPurchased;

    @Getter
    private final Timestamp purchaseTime;

    @Getter
    private final double costNow;

    @Getter
    private final double profit;

}