import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

public class BuyersEdgePlatform {
    public static void main(String[] args) throws Exception {
        HashMap<String, ProductList> products = new HashMap<>();
        Path libDir = resolveLibDir();
        File inputDir = libDir.resolve("input").toFile();
        String outputDirPath = libDir.resolve("output").toString() + File.separator;
        for (File input : inputDir.listFiles()) {
            readContractFile(input, products);
        }
        outputComparisonResults(products);
        outputResultSummary(products);
        exportResultsToCSV(products, outputDirPath);
    }

    // Locate the lib directory relative to the compiled classes rather than the current working
    // directory, so the program runs the same regardless of where it is launched from or which OS
    // it runs on. The code source location is the classpath root (the bin directory when run with
    // -cp bin), and lib is its sibling. Falls back to ./lib if the location cannot be determined.
    private static Path resolveLibDir() throws URISyntaxException {
        URL location = BuyersEdgePlatform.class.getProtectionDomain().getCodeSource().getLocation();
        if (location != null) {
            Path candidate = Paths.get(location.toURI()).getParent().resolve("lib");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        return Paths.get(System.getProperty("user.dir"), "lib");
    }

    private static void readContractFile(File input, HashMap<String, ProductList> products) {
        try (BufferedReader br = new BufferedReader(new FileReader(input))) {
            int codeIdx=0, descIdx=0, priceIdx=0;

            // Read the first line in to learn the name of the distributor
            String distributor = br.readLine().trim();

            // Read the second line and store the positional index within this contract file of Product Code, Description, and Price
            String[] tokens = br.readLine().split(",");
            for (int i = 0; i < tokens.length; i++) {
                switch (tokens[i].trim()) {
                    case "Product Code" -> codeIdx = i;
                    case "Description" -> descIdx = i;
                    case "Price" -> priceIdx = i;
                    default -> {}
                }
            }

            // Read the remaining lines and store the Product in a Map which is keyed by Product Code
            String line;
            while((line = br.readLine()) != null) {
                tokens = line.split(",");
                Float price = Float.valueOf(tokens[priceIdx].replaceAll("\\$",""));
                ProductList current = products.containsKey(tokens[codeIdx]) ? products.get(tokens[codeIdx]) : new ProductList();
                current.add(new Product(distributor, tokens[codeIdx], tokens[descIdx], price));
                products.put(tokens[codeIdx], current);
            }
        } catch (FileNotFoundException ex) {
            System.err.println("File cannot be found at " + input.getAbsolutePath());
        } catch (IOException ex) {
            System.err.println("Error accessing the file at " + input.getAbsolutePath());
        }
    }

    private static void outputComparisonResults(HashMap<String, ProductList> products) {
        // Utilize streams to iterate through all ProductLists in the map by their key and then iterate through a sorted version
        // of all Products in each ProductList and call the toString() method within ProductList
        System.out.println(products
                            .keySet()
                            .stream()
                            .map(key -> "  - Product Code: " + key + "\n" + products
                                                                            .get(key)
                                                                            .stream()
                                                                            .sorted()
                                                                            .collect(Collectors
                                                                                        .toCollection(ProductList::new))
                                                                                        .toString())
                            .collect(Collectors.joining("\n", "Results:\n", "")));
    }

    private static void outputResultSummary(HashMap<String, ProductList> products) {
        // Utilize streams to iterate through all ProductLists in the map by their key and then sum all Product 
        // prices for each distributor. Show ouput to inform the user the total price of all items if only using
        // a single distributor and then show the total price when using the cheapest distributor for each item
        // and the effective discount they receive by utilizing multiple distributors.
        StringBuilder sb = new StringBuilder();
        ArrayList<Double> distributorSums = new ArrayList<>();
        Double sum;

        sb.append("Summary:\n");
        sb.append("  - Total price to purchase one unit of all products from each distributor exclusively:\n");
        for (int i = 0; i < products.size(); i++) {
            final int x = i;

            sum = products
                    .keySet()
                    .stream()
                    .map(key -> products
                                .get(key)
                                .get(x))
                    .collect(Collectors.summingDouble(Product::getPrice));

            distributorSums.add(sum);

            sb.append("    - ");
            sb.append(products.keySet().stream().findFirst().map(key -> products.get(key).get(x).getDistributor()).get());
            sb.append(": $");
            sb.append(String.format("%.2f", sum));
            System.out.println(sb.toString());
            sb.setLength(0);
        }

        Collections.sort(distributorSums);
        
        sum = products
                .keySet()
                .stream()
                .map(key -> products
                            .get(key)
                            .stream()
                            .sorted()
                            .findFirst()
                            .get())
                .collect(Collectors.summingDouble(Product::getPrice));

        sb.append("\n  - Purchasing one unit of each product at its respective lowest price would total $");
        sb.append(String.format("%.2f", sum));
        sb.append(" which represents a ");
        sb.append(String.format("%.1f", ((distributorSums.get(0) - sum) / distributorSums.get(0)) * 100));
        sb.append("% discount.\n");
        System.out.println(sb.toString());
    }

    private static void exportResultsToCSV(HashMap<String, ProductList> products, String outputDirPath) {
        // Utilize streams to obtain the first key in the Map and get the corresponding ProductList for that key and then
        // iterate through all Products in that ProductList to print out the distributors with special delimeter, prefix, and suffix
        // to generate the first line of the created CSV file.
        String csvString = products
                            .keySet()
                            .stream()
                            .findFirst()
                            .map(key -> products
                                        .get(key)
                                        .stream()
                                        .map(prod -> prod.getDistributor())
                                        .collect(Collectors.joining(" Price,","Product Code,"," Price,Best Price\n")))
                            .get();

        // Now focus on building all of the Product data into the CSV output. This we iterate through the keySet and corresponding
        // ProductLists and make use of the toCSVOutput() method in ProductList to help format what we want to output. Use a second
        // stream sorted by price to retrieve the distributor who has the best price and add that to the end of the line for each Product Code.
        csvString += products
                        .keySet()
                        .stream()
                        .map(key -> key + products
                                            .get(key)
                                            .toCSVOutput()
                                            + ","
                                            + products
                                                .get(key)
                                                .stream()
                                                .sorted()
                                                .findFirst()
                                                .map(prod -> prod.getDistributor())
                                                .get())
                        .collect(Collectors.joining("\n", "", ""));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputDirPath + "outputByJerryHolland.csv"))) {
            bw.write(csvString);
        } catch (IOException e) {
            System.err.println("Error writing output file at " + outputDirPath);
        }
    }
}
