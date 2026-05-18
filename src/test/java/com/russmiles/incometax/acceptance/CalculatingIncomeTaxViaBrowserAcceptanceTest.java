package com.russmiles.incometax.acceptance;

import com.russmiles.incometax.adapter.in.web.WebServer;
import com.russmiles.incometax.application.service.TaxCalculator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Browser-driven acceptance tests for the web adapter.
 *
 * Drives the system as an actual user would: launches headless Chrome via
 * Selenium WebDriver, types into the form, clicks submit, and reads the
 * rendered result. The outbound port is stubbed via
 * {@link StubTaxRateRepository} so no real persistence is involved, and
 * {@link WebServer} runs on an OS-assigned port so tests stay independent.
 *
 * If Chrome (or a ChromeDriver compatible with it) is not available on the
 * machine, the tests skip with {@link Assumptions#abort} rather than fail —
 * contributors without Chrome can still run the rest of the suite.
 *
 * Requires Selenium 4.6+ (uses Selenium Manager to auto-fetch ChromeDriver).
 */
class CalculatingIncomeTaxViaBrowserAcceptanceTest {

    private final StubTaxRateRepository taxRates = new StubTaxRateRepository();
    private WebServer web;
    private WebDriver browser;

    @BeforeEach
    void launch() throws IOException {
        web = new WebServer(new TaxCalculator(taxRates));
        web.start(0);

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments(
                    "--headless=new",
                    "--no-sandbox",
                    "--disable-gpu",
                    "--window-size=1280,800");
            browser = new ChromeDriver(options);
        } catch (Throwable t) {
            web.stop();
            Assumptions.abort("Skipping browser tests — Chrome / ChromeDriver unavailable: " + t.getMessage());
        }
        browser.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterEach
    void shutdown() {
        if (browser != null) browser.quit();
        if (web != null) web.stop();
    }

    @Test
    void aUserSubmitsAnAmountAndSeesTheCalculatedTax() {
        taxRates.withRate(0.15);

        browser.get(formUrl());
        browser.findElement(By.name("amount")).sendKeys("100");
        browser.findElement(By.cssSelector("button[type=submit]")).click();

        String result = browser.findElement(By.cssSelector(".result")).getText();
        assertTrue(result.contains("Tax on 100.00 = 15.00"),
                () -> "expected formatted result on the page, got: " + result);
    }

    @Test
    void theResultShownReflectsTheCurrentTaxRate() {
        taxRates.withRate(0.40);

        browser.get(formUrl());
        browser.findElement(By.name("amount")).sendKeys("250");
        browser.findElement(By.cssSelector("button[type=submit]")).click();

        String result = browser.findElement(By.cssSelector(".result")).getText();
        assertTrue(result.contains("Tax on 250.00 = 100.00"),
                () -> "expected 250 x 0.40 = 100.00 on the page, got: " + result);
    }

    @Test
    void theLandingPageShowsTheTitleAndAnAmountField() {
        browser.get(formUrl());

        assertTrue(browser.getTitle().contains("Income Tax Calculator"),
                () -> "expected title 'Income Tax Calculator', got: " + browser.getTitle());

        WebElement amount = browser.findElement(By.name("amount"));
        assertTrue("number".equals(amount.getAttribute("type")),
                () -> "expected amount input to be type=number, got: " + amount.getAttribute("type"));
    }

    private String formUrl() {
        return "http://localhost:" + web.port() + "/";
    }
}
