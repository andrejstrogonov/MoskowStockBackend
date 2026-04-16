package org.andrejstrogonov.moskowstockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequest {

    private Double principal;
    private Double annualRate;
    private Integer years;
    private List<EarlyRepayment> earlyRepayments;
}
