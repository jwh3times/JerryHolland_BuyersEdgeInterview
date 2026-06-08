import java.util.ArrayList;
public class ProductList extends ArrayList<Product>
{
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        String result = "";

        for (int i = 0; i < this.size(); i++) {
            result += "    - " + this.get(i);
            if (i == 0)
                result += " [BEST DEAL]";
            result += "\n";
        }
        return result;
    }

    public String toCSVOutput() {
        String result = "";

        for (Product p : this) {
            result += p.toCSV();
        }
        return result;
    }
}
