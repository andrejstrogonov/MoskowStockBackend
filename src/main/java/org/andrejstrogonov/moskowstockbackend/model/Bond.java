package org.andrejstrogonov.moskowstockbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("BOND")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bond extends Instrument {

    @Column(nullable = false)
    private Double faceValue;

    @Column(nullable = false)
    private Double couponRate;

    @Column(nullable = false)
    private Integer couponFrequency;

    @Column(nullable = false)
    private LocalDateTime maturityDate;

    @Column(nullable = false)
    private Double accrualInterest;

    @Column(nullable = false)
    private Boolean isOFZ;

    private Double rating;

    private String issuer;
}
