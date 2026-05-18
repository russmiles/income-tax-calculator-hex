package com.russmiles.incometax.application.service;

import com.russmiles.incometax.application.port.in.ForCalculatingTaxes;
import com.russmiles.incometax.application.port.out.ForGettingTaxRates;

/**
 * Application service implementing the {@link ForCalculatingTaxes} use case
 * by asking a {@link ForGettingTaxRates} adapter for the applicable rate and
 * multiplying it by the supplied amount.
 */
public class TaxCalculator implements ForCalculatingTaxes {

    private final ForGettingTaxRates taxRates;

    public TaxCalculator(ForGettingTaxRates taxRates) {
        this.taxRates = taxRates;
    }

    @Override
    public double taxOn(double amount) {
        return amount * taxRates.taxRate(amount);
    }
}
