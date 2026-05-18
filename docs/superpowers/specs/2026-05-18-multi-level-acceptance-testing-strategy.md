---
title: Multi-level acceptance testing strategy for the hexagonal calculator
status: implemented (retrofit spec — written after the work to enable decision archaeology)
date: 2026-05-18
slug: multi-level-acceptance-testing-strategy
---

# Multi-level acceptance testing strategy

## Summary

The income tax calculator is structured as a hexagonal application
(ports & adapters). Acceptance tests should drive the system through
multiple inbound surfaces — the inbound port itself, the CLI adapter,
the web adapter over real HTTP, and a real browser — while always
stubbing the outbound `ForGettingTaxRates` port. Each level catches a
different class of regression; together they form a "test pyramid"
that mirrors the hexagonal layering.

## Motivation

This codebase is a teaching example for Alistair Cockburn's Heart of
Agile workshops. The *visible architectural shape* is what students
are meant to learn from. Tests are part of that visible shape: where
they live, what they exercise, and what they stub all communicate the
hex contract as clearly as the production code does.

A single "end-to-end" test suite running only through the browser
would hide that contract behind a layer of UI noise. A pure unit-test
suite would never exercise the adapters at all, leaving the hexagonal
seams uncovered. Multi-level acceptance tests make the seams visible.

## Goals

- Every inbound adapter has its own hex-style acceptance suite that
  drives the system through that adapter and stubs the outbound port.
- The application core itself has a port-level acceptance suite that
  exercises the use case without any adapter — proving the use case
  is meaningful independent of HTTP, CLI, or browser.
- All acceptance tests share one outbound stub (`StubTaxRateRepository`)
  so the "given" step reads the same in every suite.
- Acceptance suites run as part of `./mvnw test` by default. Tests
  that require optional external dependencies (a browser, in this
  case) skip cleanly via `Assumptions.abort(...)` when the dependency
  is absent — contributors without Chrome still get a green build.
- Test names read as user-visible behaviour
  (`aUserSubmitsAnAmountAndSeesTheCalculatedTax`), not implementation
  detail (`testTaxCalculatorReturnsExpectedValue`).

## Non-goals

- Production-grade load, performance, or security testing.
- Cross-browser coverage. One browser (Chrome) is enough to
  demonstrate the pattern; teaching the pattern is the goal.
- Mutation testing, contract testing against an external API, or
  property-based testing. These are good ideas but out of scope for
  the teaching example.
- A separate "integration" tier between unit and acceptance.

## Design

### Acceptance suites — one per inbound surface

| Suite | Drives through | Verifies |
|---|---|---|
| `CalculatingIncomeTaxAcceptanceTest` | `ForCalculatingTaxes` (inbound port) | Use case behaviour is meaningful with no adapter present |
| `CalculatingIncomeTaxViaCliAcceptanceTest` | `CommandLine.run(args)` | argv parsing, exit codes, stdout vs stderr routing, help flag |
| `CalculatingIncomeTaxViaWebAcceptanceTest` | `WebServer` via JDK `HttpClient` | HTTP status, content-type, query-string parsing, HTML body |
| `CalculatingIncomeTaxViaBrowserAcceptanceTest` | `WebServer` via headless Chrome + Selenium | DOM rendering, form submission, the user-visible interaction loop |

Each suite injects `StubTaxRateRepository` into a real `TaxCalculator`,
then drives the system through its target inbound adapter. No
production outbound adapter (`FixedTaxRateRepository`, a future
database adapter, an HMRC API client) is ever instantiated in an
acceptance test.

### Outbound stubbing

`StubTaxRateRepository` is the *only* outbound test double. It lives
under `src/test/java/.../acceptance/` next to the suites that use it,
exposes a fluent `withRate(...)` setter for the "given" step, and
implements `ForGettingTaxRates` exactly as a production adapter would.
No mocking framework is used; the interface is small enough that a
plain class is clearer.

### Web acceptance tests over real HTTP

The web suite starts a real `WebServer` on port `0` (OS-assigned), so
parallel test runs and developer machines can't collide on a fixed
port. The JDK's `java.net.http.HttpClient` is the client — no
additional library is needed.

### Browser acceptance tests

The browser suite uses Selenium WebDriver 4.x against headless Chrome.
Selenium Manager (built into Selenium 4.6+) auto-downloads a
ChromeDriver matching the installed Chrome version, so contributors
don't have to manage drivers themselves. If Chrome (or a
ChromeDriver) is unavailable, the suite skips via
`Assumptions.abort(...)` rather than failing.

### Test naming

Test methods are named as full sentences describing the behaviour
(`aUserSubmitsAnAmountAndSeesTheCalculatedTax`,
`changingTheRateChangesTheTaxOwed`,
`nonNumericArgumentReportsErrorAndExitsNonZero`). Suite names follow
the pattern `CalculatingIncomeTaxVia<Surface>AcceptanceTest`, where
`<Surface>` is the inbound adapter being exercised. The empty-Via
form (`CalculatingIncomeTaxAcceptanceTest`) means "through the port
directly, no adapter."

### Wiring vs acceptance

`AppTest` exercises the *wiring* (`App.wireCalculator()`) — that the
composition root assembles a working calculator from real
production-side classes (`FixedTaxRateRepository` + `TaxCalculator`).
It is not an acceptance test; it is the only place where the
production outbound adapter is exercised under test.

## Acceptance criteria

- `./mvnw test` runs all suites and reports the totals per suite.
- Each acceptance suite uses `StubTaxRateRepository` and instantiates
  the real application service (`TaxCalculator`).
- The browser suite skips cleanly when Chrome is unavailable on the
  host (no failure, no error, only "aborted / skipped").
- No acceptance test references `FixedTaxRateRepository`.
- The full suite (unit + wiring + four acceptance levels) is green.

## Open questions

- Should browser tests live in a separate Maven profile so the default
  `./mvnw test` stays sub-second? (Currently they add ~4s.)
- Should the web acceptance suite assert on full HTML structure (parse
  the response with a parser) or substring-match the rendered fragment
  (current approach)?
- When a second outbound adapter is added (e.g. a database-backed
  `ForGettingTaxRates`), should the wiring test grow to cover both
  permutations, or should we add a second wiring test per inbound
  composition root?
