import java.time.LocalDate;

public class Medicine {
    private String name;
    private String code;
    private int quantity;
    private LocalDate expiryDate;

    public Medicine(String name, String code, int quantity, LocalDate expiryDate) {
        this.name = name;
        this.code = code;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
    }

    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getName() { return name; }
    public String getCode() { return code; }
    public int getQuantity() { return quantity; }
    public LocalDate getExpiryDate() { return expiryDate; }

    public String toDataString() {
        return name + ";" + code + ";" + quantity + ";" + expiryDate;
    }

    public static Medicine fromDataString(String line) {
        String[] parts = line.split(";");
        return new Medicine(parts[0], parts[1], Integer.parseInt(parts[2]), LocalDate.parse(parts[3]));
    }
}
