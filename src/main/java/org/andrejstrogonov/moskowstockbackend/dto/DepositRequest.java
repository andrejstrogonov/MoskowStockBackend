package org.andrejstrogonov.moskowstockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {

    private Double principal;
    private Double annualRate;
    private Integer years;
    private Integer compoundingFrequency = 12;
}
