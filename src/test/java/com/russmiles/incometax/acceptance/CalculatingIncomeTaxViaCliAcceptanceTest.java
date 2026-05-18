package com.russmiles.incometax.acceptance;

import com.russmiles.incometax.adapter.in.cli.CommandLine;
import com.russmiles.incometax.application.service.TaxCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Acceptance tests for the CLI inbound adapter, hexagonal style.
 *
 * The test drives the system through the {@link CommandLine} adapter (the
 * user-visible "port" from the CLI user's perspective) and stubs the
 * outbound {@link com.russmiles.incometax.application.port.out.ForGettingTaxRates}
 * with {@link StubTaxRateRepository}. No real persistence or external system
 * is touched, and stdout/stderr are captured via injected streams so tests
 * make no global state changes.
 */
class CalculatingIncomeTaxViaCliAcceptanceTest {

    private final StubTaxRateRepository taxRates = new StubTaxRateRepository();
    private final ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
    private final ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();
    private CommandLine cli;

    @BeforeEach
    void wireCli() {
        cli = new CommandLine(
                new TaxCalculator(taxRates),
                new PrintStream(stdoutBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(stderrBuffer, true, StandardCharsets.UTF_8));
    }

    @Test
    void printsCalculatedTaxForAValidAmount() {
        taxRates.withRate(0.15);

        int exitCode = cli.run(new String[]{"100"});

        assertEquals(0, exitCode);
        assertTrue(stdout().contains("Tax on 100.00 = 15.00"),
                () -> "expected formatted result, got: " + stdout());
        assertTrue(stderr().isEmpty(), () -> "expected no stderr, got: " + stderr());
    }

    @Test
    void usesTheCurrentRateFromTheRepository() {
        taxRates.withRate(0.40);

        cli.run(new String[]{"250"});

        assertTrue(stdout().contains("Tax on 250.00 = 100.00"),
                () -> "expected 250 * 0.40 = 100.00, got: " + stdout());
    }

    @Test
    void missingArgumentPrintsUsageToStderrAndExitsNonZero() {
        int exitCode = cli.run(new String[0]);

        assertNotEquals(0, exitCode);
        assertTrue(stderr().toLowerCase().contains("usage"),
                () -> "expected usage on stderr, got: " + stderr());
        assertTrue(stdout().isEmpty(), () -> "expected no stdout, got: " + stdout());
    }

    @Test
    void nonNumericArgumentReportsErrorAndExitsNonZero() {
        int exitCode = cli.run(new String[]{"not-a-number"});

        assertNotEquals(0, exitCode);
        assertTrue(stderr().contains("not-a-number"),
                () -> "expected error mentioning the bad input, got: " + stderr());
    }

    @Test
    void helpFlagPrintsUsageToStdoutAndExitsZero() {
        int exitCode = cli.run(new String[]{"--help"});

        assertEquals(0, exitCode);
        assertTrue(stdout().toLowerCase().contains("usage"),
                () -> "expected usage on stdout, got: " + stdout());
    }

    private String stdout() {
        return stdoutBuffer.toString(StandardCharsets.UTF_8);
    }

    private String stderr() {
        return stderrBuffer.toString(StandardCharsets.UTF_8);
    }
}
