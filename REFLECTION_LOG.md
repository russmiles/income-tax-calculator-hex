# Reflection Log

Post-task reflections captured via `/reflect`. Newest entries at the bottom.

---

- **Date**: 2026-05-18
- **Agent**: Claude Opus 4.7 (1M context) — single-agent interactive session, no orchestrator
- **Task**: Built a Heart of Agile teaching example from zero — Java/Maven hexagonal income tax calculator with `ForCalculatingTaxes` / `ForGettingTaxRates` ports, `TaxCalculator` service, `FixedTaxRateRepository` outbound adapter, `WebServer` (JDK `HttpServer`) and `CommandLine` inbound adapters. Tests at four levels: unit, port-level acceptance, CLI-adapter acceptance, web-adapter acceptance over real HTTP, and browser-driven acceptance via headless Chrome / Selenium. C4 architecture docs (Mermaid). Published to `github.com/russmiles/income-tax-calculator-hex`.
- **Surprise**:
  1. The auto-mode classifier blocked an agent-initiated download of the official Maven Wrapper zip from `repo.maven.apache.org`, flagging it as "agent-inferred external code integration" — forcing a pivot to `brew install maven` then `mvn -N wrapper:wrapper`. The safety boundary fired because the *agent* chose the URL, not because the URL was suspect.
  2. Homebrew `openjdk` installs as keg-only, so `java`, `JAVA_HOME`, and `./mvnw` all fail until the user wires `JAVA_HOME` into `~/.zshrc` (or symlinks the JDK into `/Library/Java/JavaVirtualMachines`). Bash tool sessions do **not** load `~/.zshrc`, so every Java/Maven command for the rest of the session had to set `JAVA_HOME` inline.
  3. The user's working style is small, iterative refinements: they accept a generated artifact, then edit the signature in-place between turns (`BigDecimal` → `double`, `calculateTaxFor` → `taxOn`). The agent should treat its output as a first draft and re-read files before extending them rather than assuming earlier shapes hold.
- **Proposal**: If/when this project adopts the AI Literacy harness, the AGENTS.md `STYLE` section should record: *"On macOS with Homebrew `openjdk`, every Bash invocation that runs `java`, `mvn`, or `./mvnw` must export `JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home` inline — non-interactive sessions don't source `~/.zshrc`."* For Maven projects in general, prefer `mvn -N wrapper:wrapper` after a deterministic Maven install over agent-fetched wrapper archives.
- **Improvement**: When the task is "bootstrap a build tool," default to a deterministic install path (system package manager, then run the tool's own bootstrap subcommand) rather than fetching a distribution archive by URL. Cuts one round-trip with the safety classifier and matches user expectations on macOS.
- **Signal**: context
- **Constraint**: none
- **Session metadata**:
  - Duration: ~75 min (initial scaffold → first GitHub commit → CLI + browser test iterations)
  - Model tiers used: unknown (no MODEL_ROUTING.md in this project)
  - Pipeline stages completed: single-agent interactive — no spec-writer / tdd-agent / code-reviewer / integration-agent involvement
  - Agent delegation: manual
