package com.russmiles.incometax.adapter.in.web;

import com.russmiles.incometax.application.port.in.ForCalculatingTaxes;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Inbound web adapter — serves a tiny HTML form that lets a user enter an
 * income amount and see the calculated tax. Depends only on the
 * {@link ForCalculatingTaxes} port, so the underlying calculator and rate
 * adapter can be swapped without touching this class.
 *
 * Uses the JDK-bundled {@link HttpServer} to avoid pulling in a framework.
 */
public class WebServer {

    private final ForCalculatingTaxes calculator;
    private HttpServer server;

    public WebServer(ForCalculatingTaxes calculator) {
        this.calculator = calculator;
    }

    /** Starts the server on the given port (use 0 to let the OS pick one). */
    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handle);
        server.start();
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    /** Actual bound port — useful when {@link #start} was called with 0. */
    public int port() {
        return server.getAddress().getPort();
    }

    private void handle(HttpExchange exchange) throws IOException {
        Optional<Double> amount = parseAmount(exchange.getRequestURI().getRawQuery());
        String body = renderPage(amount);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private Optional<Double> parseAmount(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) return Optional.empty();
        for (String pair : rawQuery.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && "amount".equals(kv[0])) {
                try {
                    return Optional.of(Double.parseDouble(URLDecoder.decode(kv[1], StandardCharsets.UTF_8)));
                } catch (NumberFormatException ignored) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    private String renderPage(Optional<Double> amount) {
        String result = amount
                .map(a -> "<p class=\"result\">Tax on " + format(a) + " = <strong>"
                        + format(calculator.taxOn(a)) + "</strong></p>")
                .orElse("");
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <title>Income Tax Calculator</title>
                  <style>
                    body { font-family: system-ui, sans-serif; max-width: 32rem; margin: 3rem auto; }
                    label { display: block; margin-bottom: 0.5rem; }
                    input[type=number] { padding: 0.4rem; font-size: 1rem; width: 12rem; }
                    button { padding: 0.4rem 1rem; font-size: 1rem; margin-left: 0.5rem; }
                    .result { margin-top: 1.5rem; padding: 1rem; background: #f0f4f8; border-radius: 6px; }
                  </style>
                </head>
                <body>
                  <h1>Income Tax Calculator</h1>
                  <form method="get" action="/">
                    <label for="amount">Gross income</label>
                    <input id="amount" name="amount" type="number" step="0.01" min="0" required>
                    <button type="submit">Calculate</button>
                  </form>
                  %s
                </body>
                </html>
                """.formatted(result);
    }

    private static String format(double value) {
        return String.format("%.2f", value);
    }
}
