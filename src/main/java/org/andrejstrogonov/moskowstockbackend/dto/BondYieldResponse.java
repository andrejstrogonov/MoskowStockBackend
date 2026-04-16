package org.andrejstrogonov.moskowstockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BondYieldResponse {

    private Double yieldToMaturity;
    private Double currentYield;
    private Double duration;
    private Double modifiedDuration;
}
