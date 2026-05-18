package com.russmiles.incometax.adapter.out.persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FixedTaxRateRepositoryTest {

    private static final double DELTA = 1e-9;
    private final FixedTaxRateRepository repository = new FixedTaxRateRepository();

    @Test
    void returnsFifteenPercentForAnyAmount() {
        assertEquals(0.15, repository.taxRate(100.0), DELTA);
    }

    @Test
    void rateIsIndependentOfAmount() {
        assertEquals(repository.taxRate(0.0), repository.taxRate(1_000_000.0), DELTA);
    }
}
