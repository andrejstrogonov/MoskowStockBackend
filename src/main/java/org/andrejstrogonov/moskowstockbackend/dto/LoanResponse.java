package org.andrejstrogonov.moskowstockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

    private List<MonthlyPayment> monthlyPayments;
    private Double totalInterest;
    private Double totalPaid;
}
