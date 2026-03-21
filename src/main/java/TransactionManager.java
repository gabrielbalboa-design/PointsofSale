import javafx.collections.ObservableList;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TransactionManager {

    static final String TRANSACTION_FILE = "transactions.csv";
    static ArrayList<Transaction> transactions = new ArrayList<>();

    static {
        loadTransactions();
    }

    public static void log(String type, String product, int amount){
        Transaction t = new Transaction(type, product, amount);
        transactions.add(t);
        saveTransactions();
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
        saveTransactions();

        System.out.println("Sale successful: " + t);

        return true;
    }

    public static void processSale(ObservableList<Item> inventory, String name, int amount) {
    }

    public static void saveTransactions(){
        try(PrintWriter writer = new PrintWriter(new FileWriter(TRANSACTION_FILE))){

            writer.println("Timestamp,Type,Product,Quantity,Total");

            for(Transaction t : transactions){
                writer.println(
                        t.timestamp.toString() + "," +
                                t.type + "," +
                                t.product + "," +
                                t.quantity + "," +
                                t.total
                );
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void loadTransactions(){
        File file = new File(TRANSACTION_FILE);

        if(!file.exists()) return;

        try(BufferedReader reader = new BufferedReader(new FileReader(TRANSACTION_FILE))){

            String line = reader.readLine();

            while((line = reader.readLine()) != null){
                String[] parts = line.split(",", 5);
                if(parts.length >= 4){
                    LocalDateTime timestamp = LocalDateTime.parse(parts[0]);
                    String type = parts[1];
                    String product = parts[2];
                    int quantity = Integer.parseInt(parts[3]);
                    double total = parts.length > 4 ? Double.parseDouble(parts[4]) : 0.0;

                    Transaction t = new Transaction(type, product, quantity, total);
                    t.timestamp = timestamp;
                    transactions.add(t);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}