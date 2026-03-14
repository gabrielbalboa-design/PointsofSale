import java.util.ArrayList;

public class TransactionManager {

    static ArrayList<Transaction> transactions = new ArrayList<>();

    public static void log(String type, String product, int amount){

        Transaction t = new Transaction(type, product, amount);

        transactions.add(t);

        System.out.println(t);

    }

}