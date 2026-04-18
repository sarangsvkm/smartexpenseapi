package com.srg.smartexpenseapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.srg.smartexpenseapi.payload.response.DiscoveryResponse;
import org.junit.jupiter.api.Test;

class SmartDiscoveryServiceTest {

    private final SmartDiscoveryService service = new SmartDiscoveryService();

    @Test
    void returnsSafeFallbackForBlankDescription() {
        DiscoveryResponse response = service.discover("   ");

        assertEquals("MISCELLANEOUS", response.getCategory());
        assertEquals("LOW", response.getConfidence());
        assertFalse(response.getIsTaxDeductible());
    }

    @Test
    void detectsTaxRelevantRentExpenses() {
        DiscoveryResponse response = service.discover("rent payment for april");

        assertEquals("MISCELLANEOUS", response.getCategory());
        assertEquals("HIGH", response.getConfidence());
        assertTrue(response.getIsTaxDeductible());
    }
}
