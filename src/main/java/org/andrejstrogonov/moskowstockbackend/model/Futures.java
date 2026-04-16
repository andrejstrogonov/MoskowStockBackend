package org.andrejstrogonov.moskowstockbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("FUTURES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Futures extends Instrument {

    @Column(nullable = false)
    private String underlyingAsset;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Column(nullable = false)
    private Double multiplier;

    @Column(nullable = false)
    private Double volatility;

    @Column(nullable = false)
    private Double riskFreeRate;

    private Double spotPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
