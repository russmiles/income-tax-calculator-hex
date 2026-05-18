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

Tests are organised in three styles:

- **Unit tests** for individual classes (`TaxCalculatorTest`, `FixedTaxRateRepositoryTest`)
- **Acceptance tests** that drive the system through its inbound port with a
  stubbed outbound port (`acceptance/CalculatingIncomeTaxAcceptanceTest`)
- **Wiring test** for the composition root (`AppTest`)

## Requirements

- JDK 21+ (Homebrew `openjdk` works — see `~/.zshrc` for `JAVA_HOME`)
- No global Maven needed — `./mvnw` bootstraps the right version
