package org.andrejstrogonov.moskowstockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponse {

    private Double finalAmount;
    private Double totalInterest;
    private List<MonthlyData> monthlyBreakdown;
}
