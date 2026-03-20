import java.time.LocalDateTime;

public class Transaction {

    public int quantity;
    String type;
    String product;
    int amount;
    double total;
    LocalDateTime timestamp;

    public Transaction(String type, String product, int amount){
        this.type = type;
        this.product = product;
        this.amount = amount;
        this.total = 0;
        this.timestamp = LocalDateTime.now();
    }

    public Transaction(String type, String product, int amount, double total){
        this.type = type;
        this.product = product;
        this.amount = amount;
        this.total = total;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString(){
        return timestamp + " | " + type + " | " + product + " | " + amount + " | " + total;
    }
}