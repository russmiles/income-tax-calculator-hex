package com.russmiles.incometax.application.service;

import com.russmiles.incometax.application.port.out.ForGettingTaxRates;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaxCalculatorTest {

    private static final double DELTA = 1e-9;

    @Test
    void multipliesAmountByRateFromRepository() {
        ForGettingTaxRates twentyPercent = amount -> 0.20;
        TaxCalculator calculator = new TaxCalculator(twentyPercent);

        assertEquals(20.0, calculator.taxOn(100.0), DELTA);
    }

    @Test
    void zeroAmountProducesZeroTax() {
        TaxCalculator calculator = new TaxCalculator(amount -> 0.15);

        assertEquals(0.0, calculator.taxOn(0.0), DELTA);
    }

    @Test
    void passesAmountToRepositoryLookup() {
        double[] receivedAmount = {Double.NaN};
        ForGettingTaxRates spy = amount -> {
            receivedAmount[0] = amount;
            return 0.10;
        };

        new TaxCalculator(spy).taxOn(250.0);

        assertEquals(250.0, receivedAmount[0], DELTA);
    }
}
