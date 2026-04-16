package org.andrejstrogonov.moskowstockbackend;

import org.andrejstrogonov.moskowstockbackend.controller.InstrumentController;
import org.andrejstrogonov.moskowstockbackend.model.Bond;
import org.andrejstrogonov.moskowstockbackend.model.Instrument;
import org.andrejstrogonov.moskowstockbackend.model.InstrumentType;
import org.andrejstrogonov.moskowstockbackend.model.Stock;
import org.andrejstrogonov.moskowstockbackend.service.InstrumentService;
import org.andrejstrogonov.moskowstockbackend.service.ProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InstrumentControllerTest {

    @Mock
    private InstrumentService instrumentService;

    @Mock
    private ProducerService producerService;

    @InjectMocks
    private InstrumentController instrumentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetInstruments() {
        Stock instrument = new Stock();
        instrument.setId("1");
        instrument.setName("Test Stock");
        instrument.setType(InstrumentType.STOCK);

        when(instrumentService.getAllInstruments()).thenReturn(Arrays.asList(instrument));

        ResponseEntity<List<Instrument>> response = instrumentController.getInstruments(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Test Stock", response.getBody().get(0).getName());
        verify(instrumentService, times(1)).getAllInstruments();
    }

    @Test
    void testGetInstrumentsByType() {
        Bond instrument = new Bond();
        instrument.setId("1");
        instrument.setName("Test Bond");
        instrument.setType(InstrumentType.BOND);

        when(instrumentService.getInstrumentsByType(InstrumentType.BOND)).thenReturn(Arrays.asList(instrument));

        ResponseEntity<List<Instrument>> response = instrumentController.getInstruments("bond");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(InstrumentType.BOND, response.getBody().get(0).getType());
        verify(instrumentService, times(1)).getInstrumentsByType(InstrumentType.BOND);
    }

    @Test
    void testGetInstrumentsInvalidType() {
        ResponseEntity<List<Instrument>> response = instrumentController.getInstruments("invalid");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(instrumentService, never()).getInstrumentsByType(any());
    }

    @Test
    void testGetInstrument() {
        Stock instrument = new Stock();
        instrument.setId("1");
        instrument.setName("Test Instrument");

        when(instrumentService.getInstrumentById("1")).thenReturn(Optional.of(instrument));

        ResponseEntity<Instrument> response = instrumentController.getInstrument("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Instrument", response.getBody().getName());
        verify(instrumentService, times(1)).getInstrumentById("1");
    }

    @Test
    void testGetInstrumentNotFound() {
        when(instrumentService.getInstrumentById("1")).thenReturn(Optional.empty());

        ResponseEntity<Instrument> response = instrumentController.getInstrument("1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(instrumentService, times(1)).getInstrumentById("1");
    }

    @Test
    void testCreateInstrument() {
        Stock instrument = new Stock();
        instrument.setName("New Instrument");

        when(instrumentService.createInstrument(any(Instrument.class))).thenReturn(instrument);

        ResponseEntity<Instrument> response = instrumentController.createInstrument(instrument);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("New Instrument", response.getBody().getName());
        verify(instrumentService, times(1)).createInstrument(instrument);
    }

    @Test
    void testUpdateInstrument() {
        Stock updatedInstrument = new Stock();
        updatedInstrument.setId("1");
        updatedInstrument.setName("Updated Instrument");

        when(instrumentService.updateInstrument("1", updatedInstrument)).thenReturn(updatedInstrument);

        ResponseEntity<Instrument> response = instrumentController.updateInstrument("1", updatedInstrument);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Instrument", response.getBody().getName());
        verify(instrumentService, times(1)).updateInstrument("1", updatedInstrument);
        verify(producerService, times(1)).sendMarketDataUpdate(updatedInstrument);
    }

    @Test
    void testUpdateInstrumentNotFound() {
        Stock stock = new Stock();
        when(instrumentService.updateInstrument(eq("1"), any(Stock.class))).thenReturn(null);

        ResponseEntity<Instrument> response = instrumentController.updateInstrument("1", stock);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(instrumentService, times(1)).updateInstrument("1", stock);
        verify(producerService, never()).sendMarketDataUpdate(any());
    }

    @Test
    void testDeleteInstrument() {
        when(instrumentService.deleteInstrument("1")).thenReturn(true);

        ResponseEntity<Void> response = instrumentController.deleteInstrument("1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(instrumentService, times(1)).deleteInstrument("1");
    }

    @Test
    void testDeleteInstrumentNotFound() {
        when(instrumentService.deleteInstrument("1")).thenReturn(false);

        ResponseEntity<Void> response = instrumentController.deleteInstrument("1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(instrumentService, times(1)).deleteInstrument("1");
    }
}
