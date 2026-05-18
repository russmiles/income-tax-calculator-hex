package com.russmiles.incometax.adapter.out.persistence;

import com.russmiles.incometax.application.port.out.ForGettingTaxRates;

/**
 * Outbound adapter that returns a single flat tax rate regardless of amount.
 * Useful as a placeholder during early development and as a test double.
 */
public class FixedTaxRateRepository implements ForGettingTaxRates {

    @Override
    public double taxRate(double amount) {
        return 0.15;
    }
}
