package com.russmiles.incometax;

import com.russmiles.incometax.adapter.in.cli.CommandLine;

/**
 * Composition root for the CLI inbound adapter. Reuses {@link App#wireCalculator()}
 * so both entry points (web and CLI) share the same outbound wiring.
 */
public class CliApp {

    public static void main(String[] args) {
        int exitCode = new CommandLine(App.wireCalculator(), System.out, System.err).run(args);
        System.exit(exitCode);
    }
}
