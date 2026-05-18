package com.russmiles.incometax.adapter.in.cli;

import com.russmiles.incometax.application.port.in.ForCalculatingTaxes;

import java.io.PrintStream;

/**
 * Inbound CLI adapter — translates command-line arguments into a call on the
 * {@link ForCalculatingTaxes} port and writes the result to stdout (or an
 * error to stderr). Streams are injected so tests can capture output without
 * touching {@link System#out}.
 *
 * Usage:
 * <pre>
 *   tax-calc &lt;amount&gt;
 *   tax-calc --help
 * </pre>
 */
public class CommandLine {

    private static final String USAGE = "Usage: tax-calc <amount>";

    private final ForCalculatingTaxes calculator;
    private final PrintStream out;
    private final PrintStream err;

    public CommandLine(ForCalculatingTaxes calculator, PrintStream out, PrintStream err) {
        this.calculator = calculator;
        this.out = out;
        this.err = err;
    }

    /** Runs one invocation; returns the conventional process exit code. */
    public int run(String[] args) {
        if (args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) {
            out.println(USAGE);
            return 0;
        }
        if (args.length != 1) {
            err.println(USAGE);
            return 1;
        }
        try {
            double amount = Double.parseDouble(args[0]);
            double tax = calculator.taxOn(amount);
            out.printf("Tax on %.2f = %.2f%n", amount, tax);
            return 0;
        } catch (NumberFormatException e) {
            err.printf("Could not parse '%s' as a number%n", args[0]);
            return 1;
        }
    }
}
