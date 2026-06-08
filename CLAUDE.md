# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

A Java interview exercise for Buyers Edge Platform. It compares product pricing across multiple
food distributors (each supplying a CSV "contract") and determines the cheapest source per product.
There is no build tool, dependency manager, or test framework — it is plain `javac`/`java`.

## Build & run

Requires JDK 17+ (uses arrow-label switch expressions). Note the default `javac`/`java` on PATH may
be JDK 11 — a 17+ JDK (e.g. `C:/Program Files/Java/jdk-21.0.11`) is needed to compile.

The program locates `lib/` relative to the compiled classes, so it runs from any working directory:

```sh
# From the project root
javac -d bin src/*.java
java  -cp bin BuyersEdgePlatform
```

The program prints the comparison to the console and writes `lib/output/outputByJerryHolland.csv`.

## Path resolution

`BuyersEdgePlatform.resolveLibDir()` finds `lib/` as the sibling of the classpath root (the `bin/`
directory) via `getProtectionDomain().getCodeSource().getLocation()`, falling back to `./lib`. This is
CWD- and OS-independent. The `lib/` directory must therefore live alongside `bin/` at the project root.

## Architecture

Three classes in `src/`, no packages:

- **`Product`** — immutable single offer (`distributor`, `code`, `description`, `price`). `Comparable`
  by price ascending, so sorting a collection of Products puts the cheapest first. `toString()` and
  `toCSV()` produce the human-readable and CSV cell formats.
- **`ProductList extends ArrayList<Product>`** — all offers for one product code, across distributors.
  Overrides `toString()` (console block, marks index 0 as `[BEST DEAL]`) and adds `toCSVOutput()`.
- **`BuyersEdgePlatform`** — entry point and all I/O / orchestration logic.

Central data model: `HashMap<String, ProductList>` keyed by **Product Code**, grouping the same
product from every distributor together. Built in `readContractFile`, then consumed by three output
methods (`outputComparisonResults`, `outputResultSummary`, `exportResultsToCSV`), each heavy on the
Java Streams API. "Cheapest distributor" is determined throughout by `.stream().sorted().findFirst()`,
which relies on `Product`'s natural ordering — changing `Product.compareTo` changes best-price logic
everywhere.

### Two ordering assumptions to respect

- `outputResultSummary` indexes into each `ProductList` by position (`.get(x)`) to total each
  distributor's prices. This assumes every product code has the **same number of distributors in the
  same insertion order** — i.e. every distributor lists every product. Ragged/missing data breaks it.
- The CSV header row is generated from the distributor order of the *first* product code only, so all
  product rows must share that same distributor ordering for columns to line up.

## Input format

Each `lib/input/Contract_*.csv`: line 1 is the distributor name, line 2 is a header row, remaining
lines are products. Columns are located by header name (`Product Code`, `Description`, `Price`), so
column order may differ between files. Prices may include a leading `$`, which is stripped on parse.

## Conventions

- `.prettierrc.yaml` configures a Prettier Java plugin for formatting.
- Compiled `bin/`, `.claude/`, `.vscode/`, and `Instructions.docx` are gitignored.
