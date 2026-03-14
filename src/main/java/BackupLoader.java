import javafx.collections.ObservableList;
import java.io.BufferedReader;
import java.io.FileReader;

public class BackupLoader {

    public static void loadBackup(ObservableList<Item> inventory){

        try{

            BufferedReader reader = new BufferedReader(new FileReader("inventory_backup.csv"));

            String line = reader.readLine(); // skip header

            while((line = reader.readLine()) != null){

                String[] data = line.split(",");

                String name = data[0];
                String supplier = data[1];
                String contact = data[2];
                double price = Double.parseDouble(data[3]);
                int qty = Integer.parseInt(data[4]);

                inventory.add(new Item(name,supplier,contact,qty,price));

            }

            reader.close();

        }catch(Exception e){

            System.out.println("No backup found.");

        }
    }
}
