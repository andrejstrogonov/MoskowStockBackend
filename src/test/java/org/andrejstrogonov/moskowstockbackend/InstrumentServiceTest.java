package org.andrejstrogonov.moskowstockbackend;

import org.andrejstrogonov.moskowstockbackend.model.Bond;
import org.andrejstrogonov.moskowstockbackend.model.Instrument;
import org.andrejstrogonov.moskowstockbackend.model.InstrumentType;
import org.andrejstrogonov.moskowstockbackend.model.Stock;
import org.andrejstrogonov.moskowstockbackend.repository.InstrumentRepository;
import org.andrejstrogonov.moskowstockbackend.service.InstrumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InstrumentServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @InjectMocks
    private InstrumentService instrumentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllInstruments() {
        Stock instrument = new Stock();
        instrument.setId("1");
        instrument.setName("Test Stock");
        instrument.setType(InstrumentType.STOCK);

        when(instrumentRepository.findAll()).thenReturn(Arrays.asList(instrument));

        List<Instrument> instruments = instrumentService.getAllInstruments();

        assertEquals(1, instruments.size());
        assertEquals("Test Stock", instruments.get(0).getName());
        verify(instrumentRepository, times(1)).findAll();
    }

    @Test
    void testGetInstrumentsByType() {
        Bond instrument = new Bond();
        instrument.setId("1");
        instrument.setName("Test Bond");
        instrument.setType(InstrumentType.BOND);

        when(instrumentRepository.findByType(InstrumentType.BOND)).thenReturn(Arrays.asList(instrument));

        List<Instrument> instruments = instrumentService.getInstrumentsByType(InstrumentType.BOND);

        assertEquals(1, instruments.size());
        assertEquals(InstrumentType.BOND, instruments.get(0).getType());
        verify(instrumentRepository, times(1)).findByType(InstrumentType.BOND);
    }

    @Test
    void testGetInstrumentById() {
        Stock instrument = new Stock();
        instrument.setId("1");
        instrument.setName("Test Instrument");

        when(instrumentRepository.findById("1")).thenReturn(Optional.of(instrument));

        Optional<Instrument> result = instrumentService.getInstrumentById("1");

        assertTrue(result.isPresent());
        assertEquals("Test Instrument", result.get().getName());
        verify(instrumentRepository, times(1)).findById("1");
    }

    @Test
    void testCreateInstrument() {
        Stock instrument = new Stock();
        instrument.setName("New Instrument");

        when(instrumentRepository.save(any(Instrument.class))).thenReturn(instrument);

        Instrument created = instrumentService.createInstrument(instrument);

        assertNotNull(created);
        assertEquals("New Instrument", created.getName());
        verify(instrumentRepository, times(1)).save(instrument);
    }

    @Test
    void testUpdateInstrument() {
        Stock existing = new Stock();
        existing.setId("1");
        existing.setName("Old Name");

        Stock updated = new Stock();
        updated.setName("New Name");

        when(instrumentRepository.existsById("1")).thenReturn(true);
        when(instrumentRepository.save(any(Instrument.class))).thenReturn(updated);

        Instrument result = instrumentService.updateInstrument("1", updated);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        verify(instrumentRepository, times(1)).existsById("1");
        verify(instrumentRepository, times(1)).save(updated);
    }

    @Test
    void testUpdateInstrumentNotFound() {
        when(instrumentRepository.existsById("1")).thenReturn(false);

        Instrument result = instrumentService.updateInstrument("1", new Stock());

        assertNull(result);
        verify(instrumentRepository, times(1)).existsById("1");
        verify(instrumentRepository, never()).save(any());
    }

    @Test
    void testDeleteInstrument() {
        when(instrumentRepository.existsById("1")).thenReturn(true);

        boolean result = instrumentService.deleteInstrument("1");

        assertTrue(result);
        verify(instrumentRepository, times(1)).existsById("1");
        verify(instrumentRepository, times(1)).deleteById("1");
    }

    @Test
    void testDeleteInstrumentNotFound() {
        when(instrumentRepository.existsById("1")).thenReturn(false);

        boolean result = instrumentService.deleteInstrument("1");

        assertFalse(result);
        verify(instrumentRepository, times(1)).existsById("1");
        verify(instrumentRepository, never()).deleteById(any());
    }
}
