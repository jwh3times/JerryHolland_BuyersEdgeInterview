public class Product implements Comparable<Product>{
    private final String distributor;
    private final String code;
    private final String description;
    private final Float price;

    public Product (String distributor, String code, String description, float price) {
        this.distributor = distributor;
        this.code = code;
        this.description = description;
        this.price = price;
    }

    public String getDistributor() {
        return this.distributor;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public Float getPrice() {
        return this.price;
    }

    @Override
    public int compareTo(Product p) {
        return Float.compare(this.price, p.price);
    }

    @Override
    public String toString() {
        return this.distributor +
               " (" + this.description + ")" +
               ": $" + String.format("%.02f", this.price);
    }

    public String toCSV() {
        return ",$" + String.format("%.02f", this.price);
    }
}
