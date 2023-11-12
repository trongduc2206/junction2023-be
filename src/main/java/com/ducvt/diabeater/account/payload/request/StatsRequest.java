package com.ducvt.diabeater.account.payload.request;

import lombok.Data;

@Data
public class StatsRequest {
    private Long userId;
    private Float glucoseLevel;

    private Float a1cLevel;

    private Float weight;

    private Float height;
}
