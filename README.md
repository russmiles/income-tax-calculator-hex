# Income Tax Calculator (Hexagonal)

A small Java application that calculates income tax, structured using
hexagonal architecture (ports & adapters).

This codebase is a teaching example used to explore Hexagonal Architecture
as part of Alistair Cockburn's [Heart of Agile](https://heartofagile.com/)
workshops. It is deliberately tiny so that the architectural shape — not the
business logic — is what stands out.

See [`docs/architecture.md`](docs/architecture.md) for the C4 diagrams and
component layout.

## Running

Two inbound adapters share the same application core. Pick one:

**Web UI:**

```sh
./mvnw exec:java -Dexec.mainClass=com.russmiles.incometax.App
# then open http://localhost:8080
# (optional) override port:
./mvnw exec:java -Dexec.mainClass=com.russmiles.incometax.App -Dexec.args="9000"
```

**Command line:**

```sh
./mvnw -q exec:java -Dexec.mainClass=com.russmiles.incometax.CliApp -Dexec.args="100"
# Tax on 100.00 = 15.00
```

## Testing

```sh
./mvnw test
```

Tests are organised in several styles:

- **Unit tests** for individual classes (`TaxCalculatorTest`, `FixedTaxRateRepositoryTest`)
- **Wiring test** for the composition root (`AppTest`)
- **Acceptance tests** under `src/test/java/.../acceptance/`, each driving the
  system through a different inbound surface with a stubbed outbound port:
  - through the inbound **port** directly (`CalculatingIncomeTaxAcceptanceTest`)
  - through the **CLI** adapter (`CalculatingIncomeTaxViaCliAcceptanceTest`)
  - through the **web** adapter over real HTTP (`CalculatingIncomeTaxViaWebAcceptanceTest`)
  - through a real **browser** with headless Chrome via Selenium
    (`CalculatingIncomeTaxViaBrowserAcceptanceTest`) — skipped automatically if
    Chrome isn't installed

## Requirements

- JDK 21+ (Homebrew `openjdk` works — see `~/.zshrc` for `JAVA_HOME`)
- No global Maven needed — `./mvnw` bootstraps the right version

## References

- [Heart of Agile](https://heartofagile.com/) — Alistair Cockburn's framework; this codebase is used as a teaching example in those workshops.
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/) — the original 2005 article by Alistair Cockburn introducing ports & adapters.
- [The Sovereign Engineer](https://leanpub.com/thesovereignengineer) (Russ Miles, Leanpub) — the discipline of designing the environment in which human and artificial intelligence produce work worth keeping; covers harness engineering, spec-first development, and the practices used to grow this repo.
- [`ai-literacy-superpowers`](https://github.com/Habitat-Thinking/ai-literacy-superpowers) — Claude Code & GitHub Copilot plugin packaging the AI Literacy framework (harness, agent orchestration, literate programming, CUPID code review). The `REFLECTION_LOG.md` and `docs/superpowers/` artefacts in this repo are produced by it.
