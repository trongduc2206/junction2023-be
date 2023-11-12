package com.ducvt.diabeater.account.payload.response;

import com.ducvt.diabeater.account.models.Stats;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StatsResponse {
    private List<Stats> stats;
    private Map<String, String> staticAnalysis;
    private Map<String, String> dynamicAnalysis;
}
