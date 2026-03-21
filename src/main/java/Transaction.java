import java.time.LocalDateTime;

public class Transaction {

    public String type;
    public String product;
    public int quantity;
    public double total;
    public LocalDateTime timestamp;

    public Transaction(String type, String product, int quantity){
        this.type = type;
        this.product = product;
        this.quantity = quantity;
        this.total = 0;
        this.timestamp = LocalDateTime.now();
    }

    public Transaction(String type, String product, int quantity, double total){
        this.type = type;
        this.product = product;
        this.quantity = quantity;
        this.total = total;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString(){
        return timestamp + " | " + type + " | " + product + " | " + quantity + " | " + total;
    }
}