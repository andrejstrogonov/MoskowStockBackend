package org.andrejstrogonov.moskowstockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPayment {

    private Integer month;
    private Double payment;
    private Double principal;
    private Double interest;
    private Double remainingBalance;
}
