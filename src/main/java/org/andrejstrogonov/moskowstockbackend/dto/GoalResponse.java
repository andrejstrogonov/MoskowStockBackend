package org.andrejstrogonov.moskowstockbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.andrejstrogonov.moskowstockapplicationbackend.model.Portfolio;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {

    private Portfolio portfolio;
    private Double expectedReturn;
    private Boolean achievable;
    private String recommendation;
}
