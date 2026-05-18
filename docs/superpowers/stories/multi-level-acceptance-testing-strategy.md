---
spec: 2026-05-18-multi-level-acceptance-testing-strategy.md
spec_slug: multi-level-acceptance-testing-strategy
generated: 2026-05-18
generator: choice-cartographer
mode: spec
read_policy: bounded (single REFLECTION_LOG entry, no archive — project has only one prior reflection)
stories:
  - id: 1
    title: HTTP client is the JDK's java.net.http.HttpClient, not REST Assured / OkHttp / Apache HttpClient
    lens: [Optionality, Path Dependency]
    disposition: pending
    disposition_rationale: null
  - id: 2
    title: Browser driver is Selenium WebDriver, not Playwright or Cypress
    lens: [Path Dependency, Force Resolution]
    disposition: pending
    disposition_rationale: null
  - id: 3
    title: Headless Chrome is the only browser engine exercised
    lens: [Optionality, Force Resolution]
    disposition: pending
    disposition_rationale: null
  - id: 4
    title: StubTaxRateRepository lives in the acceptance package, not a shared test-fixtures module
    lens: [Boundary Drawing, Coherence]
    disposition: pending
    disposition_rationale: null
  - id: 5
    title: Fluent withRate(...) returning-this setter on the stub
    lens: [Naming/Conceptualization, Coherence]
    disposition: pending
    disposition_rationale: null
  - id: 6
    title: Per-test WebServer instance bound to OS-assigned port 0
    lens: [Force Resolution, Boundary Drawing]
    disposition: pending
    disposition_rationale: null
  - id: 7
    title: Browser tests skip via Assumptions.abort(...) when Chrome is absent rather than fail
    lens: [Force Resolution, Coherence]
    disposition: pending
    disposition_rationale: null
  - id: 8
    title: Suite naming convention CalculatingIncomeTaxVia<Surface>AcceptanceTest with an empty-Via form for the port
    lens: [Naming/Conceptualization, Coherence]
    disposition: pending
    disposition_rationale: null
  - id: 9
    title: Browser tests run inside the default ./mvnw test, not behind a Maven profile
    lens: [Force Resolution, Optionality]
    disposition: pending
    disposition_rationale: null
  - id: 10
    title: Outbound double is a hand-written class, not a Mockito / EasyMock mock
    lens: [Naming/Conceptualization, Coherence]
    disposition: pending
    disposition_rationale: null
  - id: 11
    title: The application core gets its own "empty-Via" acceptance suite as a peer of the adapter suites
    lens: [Boundary Drawing, Coherence]
    disposition: pending
    disposition_rationale: null
  - id: 12
    title: AppTest is carved out as a "wiring" tier — the sole place a production outbound adapter runs under test
    lens: [Boundary Drawing, Coherence]
    disposition: pending
    disposition_rationale: null
  - id: 13
    title: Web suite asserts on rendered HTML by substring rather than by parsing the DOM
    lens: [Force Resolution, Optionality]
    disposition: pending
    disposition_rationale: null
---

# Choice stories — multi-level acceptance testing strategy

These stories surface decisions the spec has made — including silent ones —
so they can be reviewed and dispositioned by a human. All dispositions are
`pending`; only a human editor may set them.

The spec is a retrofit (written after the implementation) for a Heart of
Agile teaching example. That framing shapes almost every choice below: the
"customer" for these tests is a workshop attendee reading the code, not a
production operations team. Decisions that would be wrong in a commercial
codebase (e.g. running browser tests in the default build) are defensible —
and possibly correct — for a teaching artefact.

---

## Story #1 — JDK HttpClient over a higher-level HTTP test library

**Context.** The web-adapter acceptance suite
(`CalculatingIncomeTaxViaWebAcceptanceTest`) drives a real `WebServer` over
real HTTP. A client is needed to issue GETs, read status codes, headers, and
bodies.

**Problem.** Java has a deep bench of HTTP clients: REST Assured (purpose-
built for HTTP test DSLs), OkHttp, Apache HttpClient, Spring's `RestClient`,
and the JDK-bundled `java.net.http.HttpClient`. Picking one constrains both
the test reading-experience and the project's external dependency surface.

**Forces.**
- The system under test has *no* HTTP framework on the production side — it
  uses the JDK's `HttpServer`. A heavyweight client library would
  asymmetrically signal "the test stack is bigger than the production stack."
- The teaching goal is to make the hex shape legible, not to demonstrate a
  testing DSL.
- Every added dependency is a `pom.xml` line a workshop attendee must read
  past before reaching the architecture.
- REST Assured's BDD-shaped DSL (`given().when().then()`) is genuinely
  expressive for sprawling REST APIs — but this API has one endpoint.

**Solution.** Use `java.net.http.HttpClient` directly, build a tiny `get(URI)`
helper inside the test class, and assert with plain JUnit `assertTrue` on
status / body substrings.

**Consequences.**
- Zero new dependencies for the web suite.
- Tests are slightly more verbose than REST Assured would be — assertions
  on content-type and body live as separate `assertTrue` calls rather than a
  chained DSL.
- If the API grows (JSON endpoints, auth, multipart), the verbosity tax
  grows linearly and the choice may need revisiting.
- The symmetry "JDK server in production / JDK client in tests" is itself a
  small piece of pedagogy.

**Lens.** Optionality (preserved by refusing a transitive-heavy dependency);
Path Dependency (the production side already chose `HttpServer`, biasing the
test side toward the matching JDK client).

**Resolves.** —

**Related-to.** Stories #2, #10 (consistent "prefer the JDK / plain Java over
a framework" pattern); Story #13 (the same minimalism shows up in how the
HTML body is asserted on).

---

## Story #2 — Selenium WebDriver over Playwright or Cypress

**Context.** The browser-acceptance suite needs to launch a real browser,
navigate to the form, type a value, click submit, and read the rendered
result.

**Problem.** Three viable contenders in 2026: Selenium WebDriver
(Java-native, oldest), Playwright for Java (Microsoft-backed, modern auto-
waiting), and Cypress (JS/TS only — would require a polyglot build).

**Forces.**
- The host project is single-language Java/Maven; adopting Cypress means
  carrying a Node toolchain, a JS test runner, and a second CI pipeline.
- Playwright for Java offers nicer ergonomics (auto-wait, network
  interception, multi-browser bundled) but is a younger Java binding and
  pulls in its own driver-management story.
- Selenium 4.6+ ships **Selenium Manager**, which auto-resolves a matching
  ChromeDriver — neutralising the historical "managing drivers is awful"
  argument that traditionally pushed people away from Selenium.
- Heart of Agile attendees are likely to have seen Selenium before; it is
  the lowest-surprise choice for a teaching artefact.

**Solution.** Selenium WebDriver 4.x against headless Chrome, relying on
Selenium Manager for driver fetch.

**Consequences.**
- Single Java/Maven toolchain preserved.
- The driver-management cliff that historically made Selenium hostile to
  contributors is delegated to Selenium Manager (see Story #7 for the
  fallback when even this fails).
- Auto-wait is not free — the suite uses an `implicitlyWait(2s)` instead.
  If the rendered DOM ever becomes asynchronous (e.g. fetch-based result
  injection), tests will need explicit waits and will be more flake-prone
  than the Playwright equivalent.
- Adopting Playwright later is mechanical (the page-object surface is
  similar), so the choice is reversible.

**Lens.** Path Dependency (single-language project shape); Force Resolution
(Selenium Manager neutralises the main historical objection).

**Resolves.** —

**Related-to.** Story #3 (browser engine), Story #7 (graceful skip when
Selenium can't find a browser).

---

## Story #3 — Headless Chrome as the sole browser engine

**Context.** Once Selenium is chosen, the browser still has to be picked.
Selenium supports Chrome, Firefox, Edge, Safari (mac only), and several
WebKit variants. The browser can run headed or headless.

**Problem.** A teaching example doesn't need cross-browser coverage; the
spec says so explicitly. But picking *one* browser still excludes everyone
who lives on the others.

**Forces.**
- Headless Chrome runs identically on macOS, Linux, and CI runners —
  important for GitHub Actions and laptop builds.
- A visible/headed browser would be more vivid in a workshop demo, but
  blocks builds run from `./mvnw test` in the background and makes the
  suite unsuitable for CI.
- Firefox/geckodriver would work equally well, but Chrome's Selenium
  Manager support has been the most reliable in 2025-26.
- Safari requires manual driver enablement and only runs on macOS.

**Solution.** Headless Chrome with `--headless=new` plus `--no-sandbox`,
`--disable-gpu`, `--window-size=1280,800`.

**Consequences.**
- Contributors without Chrome installed cannot run the browser tests at
  all — mitigated by the skip mechanism in Story #7.
- `--no-sandbox` is included to make CI sandboxes happy; on a developer
  laptop this is mildly weaker than the default, but the tests touch
  nothing sensitive.
- The non-goal "cross-browser coverage" makes this a single-axis decision;
  if the project ever needed it, the `WebDriver` interface is the right
  seam and the suite is structured to swap drivers.
- Demo-ability suffers slightly: an instructor wanting to *show* the
  browser test running would need to flip the flag.

**Lens.** Optionality (deliberately spent on one browser); Force Resolution
(CI/laptop portability trumps demo vividness).

**Resolves.** —

**Related-to.** Story #2, Story #7, Story #9.

---

## Story #4 — `StubTaxRateRepository` co-located with the acceptance suites

**Context.** The spec needs exactly one outbound test double, used by all
four acceptance suites.

**Problem.** Three plausible homes: (a) a dedicated `src/test-fixtures` /
`test-jar` module that production tests of *other* modules could depend on,
(b) a sibling `com.russmiles.incometax.testsupport` package, (c) inside
the `acceptance` package as a package-private class next to its users.

**Forces.**
- This is a single-module project with no other modules to share fixtures
  with — option (a) is over-engineered.
- Package-private visibility keeps the stub from leaking into production
  code completion, but also blocks future unit tests in other packages
  from reusing it.
- Co-location maximises the "given step reads the same in every suite"
  goal — readers don't have to chase across packages.
- The hexagonal teaching frame puts heavy weight on *where* a class lives.
  Placing the stub under `acceptance/` signals "this is a test-only
  outbound adapter, peer of the production outbound adapters."

**Solution.** `StubTaxRateRepository` is a package-private class in
`com.russmiles.incometax.acceptance`, next to the four suites that use it.

**Consequences.**
- The fixture-package question is deferred until a second consumer appears.
- If a future per-port unit test wanted the stub, it would have to either
  move up the package tree or duplicate a one-line stub — acceptable for
  this size, painful at scale.
- Reading any acceptance test, the stub is one file away in the same
  folder. The "given" step's vocabulary is locally evident.

**Lens.** Boundary Drawing (test-only adapter sits in the test-side
hexagon); Coherence (one stub, one place, one vocabulary).

**Resolves.** —

**Related-to.** Story #5, Story #10, Story #11.

---

## Story #5 — Fluent `withRate(...)` setter that returns `this`

**Context.** Each acceptance test needs to set the prevailing tax rate as
part of its Given step.

**Problem.** The stub could expose `setRate(double)` (JavaBean), a
constructor `new StubTaxRateRepository(0.15)`, an immutable
`withRate(double)` returning a *new* instance, or the chosen fluent setter
that mutates and returns `this`.

**Forces.**
- Tests share one stub instance per test (`private final
  StubTaxRateRepository taxRates = new StubTaxRateRepository();`), then
  mutate it during arrange. A constructor-only API would force
  re-construction on every rate change.
- `changingTheRateChangesTheTaxOwed` explicitly *changes* the rate
  mid-test — an immutable `withRate` returning a fresh stub would force
  re-wiring the calculator too, defeating the test's intent.
- Fluent chaining is rarely used in the suites (the tests are mostly one
  rate per test) so `return this` is mild syntactic sugar, not a load-
  bearing feature.
- The name `withRate(...)` reads better as a Given clause than
  `setRate(...)` — closer to "given a rate of 15%."

**Solution.** Mutable setter `withRate(double)` that updates the field and
returns `this`.

**Consequences.**
- Tests can change the rate at will without touching the calculator wiring.
- The stub is implicitly stateful between calls — fine for sequential
  tests, would race horribly under parallel execution against a shared
  instance (currently each test owns its own).
- The `with...` naming is a Java convention for immutable copies, so the
  signature lies slightly to a reader's expectations. Worth noting in
  review even though no one has been surprised yet.

**Lens.** Naming/Conceptualization (the name shapes the reading);
Coherence (matches the Given-When-Then narrative the suite uses).

**Resolves.** —

**Related-to.** Story #4, Story #10.

---

## Story #6 — Per-test `WebServer` instance on OS-assigned port 0

**Context.** The web and browser suites need a live HTTP server. Many test
suites would start one server per class (`@BeforeAll`) on a fixed port.

**Problem.** Choices: (a) one server per class on port 8080, (b) one server
per class on port 0, (c) one server per *test* on port 0 (chosen).

**Forces.**
- Fixed ports collide when two test JVMs run on the same machine, or when
  a developer left the production app running on 8080.
- Port 0 + asking the kernel for the assigned port eliminates that class
  of failure entirely.
- Class-level reuse is faster but couples tests through whatever state the
  server might accumulate (handlers, headers, sessions). For a small HTTP
  server with no state, this is mostly a stylistic loss.
- Per-test creation is ~milliseconds for the JDK `HttpServer` — cheap
  enough that it isn't worth the coupling.
- Parallelisation across test methods would still work with per-test
  servers; class-shared servers would need a different concurrency story.

**Solution.** `@BeforeEach` creates a fresh `WebServer`, starts it on
port `0`, and the test reads `web.port()` to build the base URI.
`@AfterEach` stops it.

**Consequences.**
- Tests are independent and parallel-safe at the method level.
- A startup-time tax of one server-creation per test. Currently invisible
  (sub-millisecond range for the JDK server).
- Sets a clear pattern: every level of acceptance suite owns its own
  surface lifecycle. The browser suite copies it.

**Lens.** Force Resolution (independence wins over speed); Boundary
Drawing (server lifecycle owned by the test, not by a shared fixture).

**Resolves.** —

**Related-to.** Story #9 (cumulative startup cost across the suite drives
the "should browser tests be in a profile?" open question).

---

## Story #7 — `Assumptions.abort(...)` on missing Chrome rather than failing

**Context.** Selenium's `new ChromeDriver(options)` throws if Chrome is not
installed or if Selenium Manager cannot fetch a compatible ChromeDriver.

**Problem.** In a strict build, this means a contributor without Chrome
sees a red bar on `./mvnw test`. The goal is for *every* contributor to be
able to run the full suite and get an honest signal.

**Forces.**
- The teaching example is shared in workshop settings; many machines won't
  have Chrome.
- A skipped test is silent unless the runner specifically surfaces it — a
  contributor *could* assume the test ran when it didn't.
- A failing test is loud and unambiguous but punishes the wrong person:
  the contributor's machine, not the code.
- JUnit 5's `Assumptions.abort(...)` reports as "aborted" (yellow/skip)
  with the supplied message, distinct from a passing test.

**Solution.** Wrap `ChromeDriver` construction in a try/catch on
`Throwable`; on failure, stop the `WebServer` (avoiding a leaked port) and
call `Assumptions.abort("...Chrome / ChromeDriver unavailable: " + ...)`.

**Consequences.**
- Contributors without Chrome get a green build with a yellow "aborted"
  note — useful and honest.
- CI must be configured to actually install Chrome; otherwise the browser
  suite silently disappears from regression coverage.
- Catching `Throwable` (vs `Exception`) protects against `Error` subtypes
  Selenium has historically thrown — slightly broader than ideal, but
  intentional here to cover the contributor-machine failure modes.
- A new failure mode is now possible: the suite is green on a CI runner
  that lost Chrome between commits. The risk is mitigated only if the CI
  log is inspected for "aborted" counts.

**Lens.** Force Resolution (kindness to contributors vs strictness of
signal); Coherence (matches the "tests run by default" stance in Story #9).

**Resolves.** —

**Related-to.** Story #2, Story #3, Story #9.

---

## Story #8 — `CalculatingIncomeTaxVia<Surface>AcceptanceTest` naming, with the empty-Via form for the port

**Context.** Four acceptance suites need distinguishable names that signal
both what they test and how.

**Problem.** Many naming conventions exist: `<Class>Test`, `<Behaviour>Spec`,
`<Adapter>IT`, BDD-flavoured `ShouldCalculateTax`. None of them spotlight
the hexagonal idea that "the *system under test* is constant; only the
*driving surface* differs."

**Forces.**
- The teaching frame requires the inbound-adapter swap to be visible from
  the file tree alone.
- A consistent prefix (`CalculatingIncomeTax`) groups the suites in IDEs
  and in `mvn test` output.
- The `Via<Surface>` suffix names the driving surface in user-readable
  English: `ViaCli`, `ViaWeb`, `ViaBrowser`.
- The port-level suite has *no* adapter — naming it `ViaPort` would lie
  (the port isn't a surface, it's *the absence of a surface*). The chosen
  convention drops the `Via` segment entirely:
  `CalculatingIncomeTaxAcceptanceTest`.
- Test methods follow a complementary sentence style
  (`aUserSubmitsAnAmountAndSeesTheCalculatedTax`,
  `nonNumericArgumentReportsErrorAndExitsNonZero`).

**Solution.** Suite name = `CalculatingIncomeTaxVia<Surface>AcceptanceTest`;
empty-Via form for the port-level suite; test methods are full
behaviour sentences in camelCase.

**Consequences.**
- The file tree is self-explaining; a workshop attendee opens the
  `acceptance/` folder and reads the architecture from the filenames.
- The "empty-Via" convention is subtle — a reader who hasn't been told
  the rule might think the file is misnamed. The spec calls this out
  explicitly to compensate.
- The convention costs nothing to extend: a future REST/JSON adapter
  becomes `CalculatingIncomeTaxViaRestAcceptanceTest`.
- Test-method names are verbose but read in CI output as English; trade-
  off against quicker `assertThat`-style names.

**Lens.** Naming/Conceptualization (names *are* the teaching);
Coherence (the convention scales to future inbound adapters).

**Resolves.** —

**Related-to.** Story #11 (the port-level suite the empty-Via form refers
to).

---

## Story #9 — Browser tests in the default `./mvnw test`, not behind a profile

**Context.** The browser suite adds ~4s to a build that is otherwise sub-
second. A common Maven idiom would be to gate slow / integration-flavoured
tests behind `mvn verify` or `-P browser`.

**Problem.** The spec itself flags this as an open question. The current
choice is "everything runs by default."

**Forces.**
- Defaulting browser tests to *off* means a contributor's first
  `./mvnw test` doesn't exercise the user-visible path at all. They will
  rarely opt in.
- 4 seconds is well under the threshold (commonly ~10s) at which
  developers notice a build is slow.
- Profile-gating splits the suite mentally: "real tests" and "extra
  tests." That contradicts the spec's framing of all four tiers as equal
  acceptance suites.
- A failing browser test must be loud enough to fix; demoting it behind a
  profile silently demotes its perceived importance.
- The graceful skip (Story #7) means the cost to a contributor without
  Chrome is zero seconds, not 4.

**Solution.** No profile. All four acceptance suites run in `./mvnw test`.

**Consequences.**
- The build remains "one command, one signal" — a teaching virtue.
- If the suite ever grows beyond a few seconds (e.g. dozens of browser
  scenarios), this choice will need revisiting; the spec's open question
  is the early-warning system.
- Coupling the default build to Chrome availability is partially
  mitigated by the skip, but CI without Chrome will quietly lose a tier.

**Lens.** Force Resolution (speed budget vs signal cohesion); Optionality
(profile-gating is one Maven config away, so reversal is cheap).

**Resolves.** —

**Related-to.** Story #6 (cumulative per-test server startup is the speed
risk); Story #7 (the skip mechanism is what makes "default-on" tolerable).

---

## Story #10 — Hand-written `StubTaxRateRepository`, no mocking framework

**Context.** A test double for `ForGettingTaxRates` is needed by every
acceptance suite.

**Problem.** Industry-standard practice is to reach for Mockito (or
EasyMock, JMockit) and write `when(rates.taxRate(anyDouble())).thenReturn(0.15)`.
The alternative is a tiny hand-written class.

**Forces.**
- `ForGettingTaxRates` has one method — Mockito would save ~3 lines per
  test and add a `~/.m2` dependency.
- Mock setups read as test-framework DSL; a hand-written stub reads as
  Java. For a teaching example, fewer DSLs to learn is a feature.
- A hand-written stub doubles as a *demonstration* of the outbound-port
  contract — students see exactly what implementing `ForGettingTaxRates`
  looks like.
- Mocks make verifying interactions (`verify(rates).taxRate(100.0)`)
  trivial; the chosen approach can only observe state. The suites do not
  currently need interaction verification.
- A hand-written stub keeps the test fixture's *static type* equal to the
  production interface — no proxy-class indirection in stack traces.

**Solution.** `StubTaxRateRepository implements ForGettingTaxRates` with
explicit state and a fluent setter.

**Consequences.**
- Zero mocking-framework dependency in the project.
- If the outbound port grew to many methods, the hand-written approach
  would become more painful and would deserve revisiting.
- The stub *is* a piece of teaching material — the file is short, named,
  and inspectable, unlike a Mockito anonymous setup.

**Lens.** Naming/Conceptualization (the test double is itself an example
of the contract); Coherence (matches the "plain Java, no framework" stance
already present in Story #1).

**Resolves.** —

**Related-to.** Story #1, Story #4, Story #5.

---

## Story #11 — The application core gets its own "empty-Via" acceptance suite as a peer of the adapter suites

**Context.** Three of the four acceptance suites drive the system through
an inbound adapter (CLI, web, browser). The fourth
(`CalculatingIncomeTaxAcceptanceTest`) drives directly through the inbound
port with no adapter.

**Problem.** A more conventional layout would call this fourth suite a
"unit test of `TaxCalculator`" and put it in
`application/service/TaxCalculatorTest.java` — which in fact already
exists. So why a *second* port-level suite under `acceptance/`?

**Forces.**
- The conventional unit test asserts implementation behaviour
  (`TaxCalculator`-specific). The acceptance suite asserts *use-case*
  behaviour through `ForCalculatingTaxes` — the contract a future second
  implementation would also have to satisfy.
- Treating the port as a first-class testing surface drives home the
  hexagonal point that *"the application is meaningful without any
  adapter."* Removing this suite weakens the teaching narrative.
- Cost: some test logic is duplicated between `TaxCalculatorTest` and
  `CalculatingIncomeTaxAcceptanceTest`. For a tiny calculator, the
  duplication is negligible.
- The pattern naturally extends: if `TaxCalculator` were ever replaced
  (e.g. by a `BandedTaxCalculator`), the acceptance suite would still
  pass unmodified.

**Solution.** Keep the port-level acceptance suite as a peer of the three
adapter-level suites, alongside the unit test under
`application/service/`. The empty-Via naming (Story #8) acknowledges the
relationship.

**Consequences.**
- Acceptance suite count is four — visible symmetry with the four inbound
  surfaces (port + 3 adapters), an architectural mnemonic.
- A small amount of assertion overlap with the unit test. Defensible for
  pedagogy; would deserve consolidation in a non-teaching codebase.

**Lens.** Boundary Drawing (port is a testing surface in its own right);
Coherence (without this suite, the "multi-level" framing loses one level).

**Resolves.** —

**Related-to.** Story #8, Story #12.

---

## Story #12 — `AppTest` is a "wiring" tier — the sole place a production outbound adapter runs under test

**Context.** The spec explicitly carves out `AppTest` from the acceptance
taxonomy: *"It is not an acceptance test; it is the only place where the
production outbound adapter is exercised under test."*

**Problem.** With four acceptance suites and a unit suite, where does
"does the composition root actually wire things correctly?" go? Options
include: (a) absorb into an acceptance suite that uses the real adapter,
(b) skip entirely and trust the unit + acceptance layers, (c) a dedicated
single-test wiring check on `App.wireCalculator()`.

**Forces.**
- Acceptance suites stub the outbound port deliberately, so they would
  not catch a misconfigured `App.wireCalculator()`.
- Without *some* check, a refactor that drops `FixedTaxRateRepository`
  from the wiring goes uncaught until runtime.
- A full end-to-end test through real HTTP *and* the real repository
  would catch wiring bugs, but mixes two concerns (HTTP correctness and
  composition correctness) and creates a heavier failure surface.
- Naming the tier "wiring" rather than "integration" or "smoke" lines up
  with the hexagonal vocabulary: this is about composition-root
  correctness, not about any external system.

**Solution.** Single `AppTest` that calls `App.wireCalculator()` and asserts
the resulting `ForCalculatingTaxes` returns the expected value (15.0 on
100.0, given the fixed 0.15 rate in `FixedTaxRateRepository`).

**Consequences.**
- A two-line assertion catches the entire class of "composition root is
  wrong" bugs.
- The acceptance suites stay strictly hexagonal — no production outbound
  adapter contaminates them.
- A new "tier" vocabulary (unit / wiring / acceptance) appears in the
  project that workshop attendees must absorb.
- If a second composition root is introduced (e.g. `CliApp`, which
  already exists per the architecture doc), it deserves its own wiring
  test by the same logic — currently not present and not noted in the
  spec.

**Lens.** Boundary Drawing (wiring is a separate concern from acceptance);
Coherence (the carve-out keeps acceptance suites pure).

**Resolves.** —

**Related-to.** Story #4 (StubTaxRateRepository is the *only* outbound
double in acceptance; AppTest is the *only* place the real one runs);
Story #11.

---

## Story #13 — Web suite asserts on rendered HTML by substring rather than by parsing the DOM

**Context.** The web acceptance suite asserts that the response body
contains, e.g., `"Tax on 100.00 = <strong>15.00</strong>"`. The browser
suite uses Selenium's element selectors against `.result`.

**Problem.** The web suite could instead parse the HTML (Jsoup, JTidy) and
assert structurally — `document.select(".result").text().equals(...)`.
Substring matching couples assertions to surface-level rendering details
including HTML tags.

**Forces.**
- Parsing adds a dependency (Jsoup) for one assertion shape.
- The browser suite *already* does structural assertions (via
  `By.cssSelector(".result")`), so the web suite's substring-matching
  asserts a different and weaker property: "the bytes on the wire
  contain this string."
- A fragile assertion like `<strong>15.00</strong>` will break when the
  template is restyled — but in this codebase, the template lives in
  `WebServer` and is rarely touched, and the test is the only consumer.
- The spec's open questions explicitly flag this: *"Should the web
  acceptance suite assert on full HTML structure (parse the response
  with a parser) or substring-match the rendered fragment (current
  approach)?"*

**Solution.** Substring matching against the response body.

**Consequences.**
- Zero parsing dependency; the web suite stays slim.
- Tests will break on template/markup changes that don't change behaviour
  (e.g. swapping `<strong>` for `<b>`).
- The web suite asserts a slightly different property from the browser
  suite (raw bytes vs rendered DOM). For a small app this is a feature
  (covers different failure modes); at scale it would become confusing
  duplication.
- The spec's open question is the explicit hook for revisiting this.

**Lens.** Force Resolution (dependency-light vs assertion-robust);
Optionality (the spec records this as an open question, leaving the door
open).

**Resolves.** —

**Related-to.** Story #1 (same minimalism around HTTP-side tooling).

---

*End of stories. All dispositions remain `pending`; only a human editor
may set them. The cartographer does not raise objections — that is the
diaboli's role — and no diaboli record exists for this retrofit spec.*
