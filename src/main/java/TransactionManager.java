import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TransactionManager {

    public static ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    public static void log(String type, String product, int amount){
        Transaction t = new Transaction(type, product, amount);
        transactions.add(t);
        System.out.println(t);
    }

    public static boolean processSale(Item item, int quantity){

        if (item == null) {
            System.out.println("Product not found.");
            return false;
        }

        if (!item.reduceStock(quantity)) {
            System.out.println("Not enough stock for " + item.name);
            return false;
        }

        double total = item.price * quantity;

        Transaction t = new Transaction("SALE", item.name, quantity, total);
        transactions.add(t);

        System.out.println("Sale successful: " + t);

        return true;
    }

    public static void processSale(ObservableList<Item> inventory, String name, int amount) {

        for(Item item : inventory){

            if(item.name.equalsIgnoreCase(name)){

                boolean success = processSale(item, amount);

                if(!success){
                    System.out.println("Sale failed.");
                }

                return;
            }
        }

        System.out.println("Item not found.");
    }
}