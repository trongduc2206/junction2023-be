package com.ducvt.diabeater.account.payload.response;

import lombok.Data;

@Data
public class StatsFrontPage {
    private String metricName;
    private Float latest;
    private Float average;
    private String dynamicAnalysis;
    private String staticAnalysis;
    private Float change;
    private String unit;
    private String description;
}
