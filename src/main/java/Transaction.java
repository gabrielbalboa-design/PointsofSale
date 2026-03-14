import java.time.LocalDateTime;

public class Transaction {

    String type;
    String product;
    int quantity;
    LocalDateTime timestamp;

    public Transaction(String type, String product, int quantity){

        this.type = type;
        this.product = product;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();

    }

    @Override
    public String toString(){

        return timestamp + " | " + type + " | " + product + " | " + quantity;

    }

}