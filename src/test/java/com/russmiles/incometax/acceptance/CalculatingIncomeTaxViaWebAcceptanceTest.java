package com.russmiles.incometax.acceptance;

import com.russmiles.incometax.adapter.in.web.WebServer;
import com.russmiles.incometax.application.service.TaxCalculator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Acceptance tests for the web inbound adapter, hexagonal style.
 *
 * The test drives the system end-to-end through the {@link WebServer} adapter
 * — a real HTTP server is started on an OS-assigned port and exercised with
 * the JDK {@link HttpClient}. The outbound port is stubbed via
 * {@link StubTaxRateRepository}, so no real persistence or external system is
 * involved. Each test gets its own server instance to keep tests independent.
 */
class CalculatingIncomeTaxViaWebAcceptanceTest {

    private final StubTaxRateRepository taxRates = new StubTaxRateRepository();
    private WebServer web;
    private HttpClient http;
    private URI baseUri;

    @BeforeEach
    void startServer() throws IOException {
        web = new WebServer(new TaxCalculator(taxRates));
        web.start(0); // 0 → OS picks a free port, avoiding collisions between tests
        baseUri = URI.create("http://localhost:" + web.port() + "/");
        http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @AfterEach
    void stopServer() {
        web.stop();
    }

    @Test
    void rootServesHtmlFormForSubmittingAnAmount() throws Exception {
        HttpResponse<String> response = get(baseUri);

        assertEquals(200, response.statusCode());
        assertTrue(contentType(response).contains("text/html"),
                () -> "expected text/html, got: " + contentType(response));
        assertTrue(response.body().contains("<form"),
                () -> "expected a form in the page, got:\n" + response.body());
        assertTrue(response.body().contains("name=\"amount\""),
                () -> "expected an 'amount' input field");
    }

    @Test
    void submittingAnAmountReturnsTheCalculatedTaxInTheBody() throws Exception {
        taxRates.withRate(0.15);

        HttpResponse<String> response = get(baseUri.resolve("?amount=100"));

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Tax on 100.00 = <strong>15.00</strong>"),
                () -> "expected formatted result, got:\n" + response.body());
    }

    @Test
    void changingTheRateChangesTheTaxShownInTheResponse() throws Exception {
        taxRates.withRate(0.40);

        HttpResponse<String> response = get(baseUri.resolve("?amount=250"));

        assertTrue(response.body().contains("Tax on 250.00 = <strong>100.00</strong>"),
                () -> "expected 250 * 0.40 = 100.00, got:\n" + response.body());
    }

    @Test
    void invalidAmountFallsBackToTheBareFormWithNoResult() throws Exception {
        HttpResponse<String> response = get(baseUri.resolve("?amount=not-a-number"));

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("<form"));
        assertFalse(response.body().contains("Tax on"),
                () -> "expected no result block for invalid input, got:\n" + response.body());
    }

    private HttpResponse<String> get(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();
        return http.send(request, BodyHandlers.ofString());
    }

    private static String contentType(HttpResponse<?> response) {
        return response.headers().firstValue("Content-Type").orElse("");
    }
}
