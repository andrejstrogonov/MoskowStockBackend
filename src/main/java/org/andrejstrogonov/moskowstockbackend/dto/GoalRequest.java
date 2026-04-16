package org.andrejstrogonov.moskowstockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {

    private Double capital;
    private Double targetAnnualReturnPercent;
    private Integer years;
    private RiskLevel riskLevel;
}
