package org.andrejstrogonov.moskowstockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAnalysisResponse {

    private Double fairValue;
    private Recommendation recommendation;
    private RiskLevel riskLevel;
    private SectorComparison sectorComparison;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorComparison {
        private Double avgPE;
        private Double avgROE;
        private Double avgDividendYield;
    }
}
