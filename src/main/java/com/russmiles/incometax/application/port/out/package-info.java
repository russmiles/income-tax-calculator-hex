/**
 * Driven (outbound) ports — interfaces the application needs from the outside.
 *
 * Examples: TaxBandRepository, TaxYearProvider. Implemented by outbound
 * adapters (persistence, external APIs). The application depends on the
 * interface; the adapter depends on the interface and provides the
 * implementation — dependency inversion.
 */
package com.russmiles.incometax.application.port.out;
