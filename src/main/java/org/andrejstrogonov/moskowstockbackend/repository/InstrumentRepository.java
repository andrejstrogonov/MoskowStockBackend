package org.andrejstrogonov.moskowstockbackend.repository;

import org.andrejstrogonov.moskowstockbackend.model.Instrument;
import org.andrejstrogonov.moskowstockbackend.model.InstrumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, String> {

    List<Instrument> findByType(InstrumentType type);
}
