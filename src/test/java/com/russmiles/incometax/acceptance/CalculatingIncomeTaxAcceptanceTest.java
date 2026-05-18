package com.russmiles.incometax.acceptance;

import com.russmiles.incometax.application.port.in.ForCalculatingTaxes;
import com.russmiles.incometax.application.service.TaxCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Acceptance tests for the system's tax-calculation behaviour.
 *
 * Style: hexagonal. The test drives the application through the inbound port
 * ({@link ForCalculatingTaxes}) and replaces the outbound port with a stub
 * ({@link StubTaxRateRepository}). No production adapter is touched and no
 * I/O happens, so the tests describe business behaviour, not infrastructure.
 */
class CalculatingIncomeTaxAcceptanceTest {

    private static final double DELTA = 1e-9;

    private final StubTaxRateRepository taxRates = new StubTaxRateRepository();
    private final ForCalculatingTaxes system = new TaxCalculator(taxRates);

    @Test
    void taxOnIncomeIsIncomeMultipliedByTheCurrentRate() {
        taxRates.withRate(0.15);

        double tax = system.taxOn(100.0);

        assertEquals(15.0, tax, DELTA);
    }

    @Test
    void taxOnZeroIncomeIsZero() {
        taxRates.withRate(0.15);

        double tax = system.taxOn(0.0);

        assertEquals(0.0, tax, DELTA);
    }

    @Test
    void changingTheRateChangesTheTaxOwed() {
        taxRates.withRate(0.20);
        double atTwentyPercent = system.taxOn(100.0);

        taxRates.withRate(0.40);
        double atFortyPercent = system.taxOn(100.0);

        assertEquals(20.0, atTwentyPercent, DELTA);
        assertEquals(40.0, atFortyPercent, DELTA);
    }

    @Test
    void aZeroRateProducesNoTaxRegardlessOfIncome() {
        taxRates.withRate(0.0);

        assertEquals(0.0, system.taxOn(1_000_000.0), DELTA);
    }
}
