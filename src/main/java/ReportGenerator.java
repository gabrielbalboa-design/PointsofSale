import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import java.io.FileWriter;
import java.io.IOException;

public class ReportGenerator {

    public static void generateReport(ObservableList<Item> inventory){

        try{

            FileWriter writer = new FileWriter("inventory_report.csv");

            writer.append("=== INVENTORY REPORT ===\n");
            writer.append("Product,Supplier,Price,Quantity,Total Value\n");

            double totalValue = 0;

            for(Item item : inventory){

                double value = item.getTotalValue();
                totalValue += value;

                writer.append(item.name).append(",")
                        .append(item.supplier).append(",")
                        .append(String.valueOf(item.price)).append(",")
                        .append(String.valueOf(item.quantity)).append(",")
                        .append(String.valueOf(value)).append("\n");
            }

            writer.append("\nTotal Inventory Value,,,,")
                    .append(String.valueOf(totalValue))
                    .append("\n\n");

            writer.append("=== TRANSACTION HISTORY ===\n");
            writer.append("Timestamp,Action,Product,Quantity\n");

            for(Transaction t : TransactionManager.transactions){

                writer.append(t.timestamp.toString()).append(",")
                        .append(t.type).append(",")
                        .append(t.product).append(",")
                        .append(String.valueOf(t.quantity)).append("\n");
            }

            writer.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Report Generated Successfully");
            alert.setContentText("inventory_report.csv created.");

            alert.showAndWait();

        }catch(IOException e){

            e.printStackTrace();

        }
    }
}