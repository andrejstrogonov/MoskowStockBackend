package org.andrejstrogonov.moskowstockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlackScholesResponse {

    private Double optionPrice;
    private Double delta;
    private Double gamma;
    private Double theta;
    private Double vega;
    private Double rho;
}
