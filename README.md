# Buyers Edge Platform — Distributor Price Comparison

A small Java program that compares product pricing across multiple food distributors and
identifies the cheapest source for each product. Built as an interview exercise for Buyers
Edge Platform.

## What it does

Each distributor supplies a contract in CSV form listing their product codes, descriptions,
and prices. The program:

1. Reads every contract file in `lib/input/`, keying each product by its **Product Code** so
   the same product from different distributors is grouped together.
2. Prints a per-product comparison to the console, flagging the cheapest offer as the
   `[BEST DEAL]`.
3. Prints a summary showing the total cost of buying one of every product from each
   distributor exclusively, versus the total when buying each product from its cheapest
   distributor — along with the effective discount that mixed sourcing yields.
4. Writes a consolidated comparison table to `lib/output/outputByJerryHolland.csv`, with one
   row per product, a price column per distributor, and a **Best Price** column naming the
   winning distributor.

## Project structure

```
src/
  BuyersEdgePlatform.java   Entry point: reads contracts, runs comparison, exports CSV
  Product.java              A single distributor's offer for a product (Comparable by price)
  ProductList.java          An ArrayList<Product> with console + CSV formatting helpers
lib/
  input/                    Distributor contract CSVs consumed by the program
  output/                   Generated comparison CSV and a screenshot of a sample run
```

## Input format

Each contract CSV starts with the distributor's name, followed by a header row, then one row
per product. Column order may vary between files — the program locates `Product Code`,
`Description`, and `Price` by their header names rather than by fixed position.

```csv
Davidson Produce
Product Code,Description,Price
322-2295,Blackberry,33
652-1962,Blueberry,16
56356,Grape,7
1045,Papaya,26
```

## Output

Console output lists each product with every distributor's price (cheapest first, marked
`[BEST DEAL]`) and a summary of total/discount figures. The CSV export looks like:

```csv
Product Code,Davidson Produce Price,Fox Foods Price,Roberts Fresh Food Price,Thompson Fresh Price,Best Price
322-2295,$33.00,$31.00,$32.00,$35.00,Fox Foods
652-1962,$16.00,$17.00,$15.00,$17.00,Roberts Fresh Food
56356,$7.00,$8.00,$9.50,$7.50,Davidson Produce
1045,$26.00,$27.00,$25.00,$24.00,Thompson Fresh
```

## Building and running

Requires JDK 17+ (the code uses switch expressions with arrow labels).

The program locates its `lib/` directory relative to the compiled classes, so it can be run
from any working directory:

```sh
# From the project root
javac -d bin src/*.java
java  -cp bin BuyersEdgePlatform
```

The comparison prints to the console and the generated CSV is written to
`lib/output/outputByJerryHolland.csv`.

## Notes

- `.prettierrc.yaml` configures a Prettier Java plugin for source formatting.
- Compiled `.class` files (`bin/`) are not tracked in this repository.
