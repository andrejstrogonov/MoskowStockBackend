package org.andrejstrogonov.moskowstockbackend.controller;

import org.andrejstrogonov.moskowstockbackend.model.Instrument;
import org.andrejstrogonov.moskowstockbackend.model.InstrumentType;
import org.andrejstrogonov.moskowstockbackend.service.InstrumentService;
import org.andrejstrogonov.moskowstockbackend.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    @Autowired
    private InstrumentService instrumentService;

    @Autowired(required = false)
    private ProducerService producerService;

    @GetMapping
    public ResponseEntity<List<Instrument>> getInstruments(@RequestParam(required = false) String type) {
        if (type != null) {
            try {
                InstrumentType instrumentType = InstrumentType.valueOf(type.toUpperCase());
                return ResponseEntity.ok(instrumentService.getInstrumentsByType(instrumentType));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(instrumentService.getAllInstruments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Instrument> getInstrument(@PathVariable String id) {
        return instrumentService.getInstrumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Instrument> createInstrument(@RequestBody Instrument instrument) {
        Instrument created = instrumentService.createInstrument(instrument);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Instrument> updateInstrument(@PathVariable String id, @RequestBody Instrument instrument) {
        Instrument updated = instrumentService.updateInstrument(id, instrument);
        if (updated != null) {
            // Send update to RabbitMQ for async processing
            if (producerService != null) {
                producerService.sendMarketDataUpdate(updated);
            }
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstrument(@PathVariable String id) {
        if (instrumentService.deleteInstrument(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
