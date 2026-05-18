package com.russmiles.incometax.acceptance;

import com.russmiles.incometax.application.port.out.ForGettingTaxRates;

/**
 * Test-only outbound adapter — a configurable in-memory stand-in for any real
 * {@link ForGettingTaxRates} implementation. Acceptance tests use this to set
 * the prevailing rate as part of a Given step, without coupling to a concrete
 * production adapter.
 */
class StubTaxRateRepository implements ForGettingTaxRates {

    private double rate;

    StubTaxRateRepository withRate(double rate) {
        this.rate = rate;
        return this;
    }

    @Override
    public double taxRate(double amount) {
        return rate;
    }
}
