package org.andrejstrogonov.moskowstockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyData {

    private Integer month;
    private Double balance;
    private Double interestEarned;
}
