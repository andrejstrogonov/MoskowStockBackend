package org.andrejstrogonov.moskowstockbackend.service;

import org.andrejstrogonov.moskowstockbackend.model.Instrument;
import org.andrejstrogonov.moskowstockbackend.model.InstrumentType;
import org.andrejstrogonov.moskowstockbackend.repository.InstrumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InstrumentService {

    @Autowired
    private InstrumentRepository instrumentRepository;

    public List<Instrument> getAllInstruments() {
        return instrumentRepository.findAll();
    }

    public List<Instrument> getInstrumentsByType(InstrumentType type) {
        return instrumentRepository.findByType(type);
    }

    public Optional<Instrument> getInstrumentById(String id) {
        return instrumentRepository.findById(id);
    }

    public Instrument createInstrument(Instrument instrument) {
        return instrumentRepository.save(instrument);
    }

    public Instrument updateInstrument(String id, Instrument instrument) {
        if (instrumentRepository.existsById(id)) {
            instrument.setId(id);
            return instrumentRepository.save(instrument);
        }
        return null;
    }

    public boolean deleteInstrument(String id) {
        if (instrumentRepository.existsById(id)) {
            instrumentRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
