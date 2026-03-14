import javafx.collections.ObservableList;
import java.io.FileWriter;
import java.io.IOException;

public class BackupManager {

    public static void saveBackup(ObservableList<Item> inventory){

        try{

            FileWriter writer = new FileWriter("inventory_backup.csv");

            writer.append("Product,Supplier,Contact,Price,Quantity\n");

            for(Item item : inventory){

                writer.append(item.name).append(",")
                        .append(item.supplier).append(",")
                        .append(item.supplierContact).append(",")
                        .append(String.valueOf(item.price)).append(",")
                        .append(String.valueOf(item.quantity)).append("\n");

            }

            writer.close();

            System.out.println("Backup saved.");

        }catch(IOException e){

            e.printStackTrace();

        }
    }
}
