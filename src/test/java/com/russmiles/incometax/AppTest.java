package com.russmiles.incometax;

import com.russmiles.incometax.application.port.in.ForCalculatingTaxes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppTest {

    private static final double DELTA = 1e-9;

    @Test
    void wiresCalculatorWithFixedRateRepository() {
        ForCalculatingTaxes calculator = App.wireCalculator();

        assertNotNull(calculator);
        assertEquals(15.0, calculator.taxOn(100.0), DELTA);
    }
}
