package com.russmiles.incometax;

import com.russmiles.incometax.adapter.in.web.WebServer;
import com.russmiles.incometax.adapter.out.persistence.FixedTaxRateRepository;
import com.russmiles.incometax.application.port.in.ForCalculatingTaxes;
import com.russmiles.incometax.application.port.out.ForGettingTaxRates;
import com.russmiles.incometax.application.service.TaxCalculator;

import java.io.IOException;

/**
 * Composition root — wires outbound adapters into application services and
 * exposes the system through inbound adapters (currently a web UI).
 */
public class App {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        ForCalculatingTaxes calculator = wireCalculator();

        WebServer web = new WebServer(calculator);
        web.start(port);
        System.out.printf("Income Tax Calculator listening on http://localhost:%d%n", web.port());
    }

    /** Package-private so tests can exercise the wiring without starting a server. */
    static ForCalculatingTaxes wireCalculator() {
        ForGettingTaxRates taxRates = new FixedTaxRateRepository();
        return new TaxCalculator(taxRates);
    }
}
