package org.andrejstrogonov.moskowstockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BondYieldRequest {

    private Double faceValue;
    private Double currentPrice;
    private Double couponRate;
    private Integer couponFrequency;
    private Double yearsToMaturity;
}
