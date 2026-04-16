package org.andrejstrogonov.moskowstockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAnalysisRequest {

    private Double dividendYield;
    private Double eps;
    private Double pe;
    private Double roe;
    private Double dividendPayout;
    private Double marketCap;
    private String sector;
}
