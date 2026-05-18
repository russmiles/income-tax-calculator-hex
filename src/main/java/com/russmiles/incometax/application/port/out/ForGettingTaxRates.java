package com.russmiles.incometax.application.port.out;

/**
 * Driven port — what the application needs from the outside in order to know
 * the current tax rates. An outbound adapter (database, config file, HMRC API,
 * etc.) provides the implementation; the application depends only on this
 * interface.
 */
public interface ForGettingTaxRates {

    public double taxRate(double amount);
}
