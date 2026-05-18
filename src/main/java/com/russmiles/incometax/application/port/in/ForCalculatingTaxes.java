package com.russmiles.incometax.application.port.in;

/**
 * Driving port — what the application offers callers that need to calculate
 * income tax. Adapters (web, CLI, tests) depend on this interface; an
 * application service provides the implementation.
 */
public interface ForCalculatingTaxes {

    double taxOn(double amount);
}
