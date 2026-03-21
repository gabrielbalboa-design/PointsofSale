import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ReportGenerator {

    public static void generateReport(ObservableList<Item> inventory){

        try{

            // ================= MAIN REPORT =================
            FileWriter writer = new FileWriter("inventory_report.csv");

            // ===== INVENTORY =====
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

            // ===== SALES SUMMARY =====
            double totalRevenue = 0;
            int totalItemsSold = 0;

            for(Transaction t : TransactionManager.transactions){
                if(t.type.equals("SALE")){
                    totalRevenue += t.total;
                    totalItemsSold += t.quantity;
                }
            }

            writer.append("=== SALES SUMMARY ===\n");
            writer.append("Total Items Sold," + totalItemsSold + "\n");
            writer.append("Total Revenue," + totalRevenue + "\n\n");

            // ===== DAILY SALES =====
            Map<LocalDate, Integer> itemsPerDay = new HashMap<>();
            Map<LocalDate, Double> revenuePerDay = new HashMap<>();

            for(Transaction t : TransactionManager.transactions){
                if(t.type.equals("SALE")){

                    LocalDate date = t.timestamp.toLocalDate();

                    itemsPerDay.put(
                            date,
                            itemsPerDay.getOrDefault(date, 0) + t.quantity
                    );

                    revenuePerDay.put(
                            date,
                            revenuePerDay.getOrDefault(date, 0.0) + t.total
                    );
                }
            }

            writer.append("=== DAILY SALES ===\n");
            writer.append("Date,Items Sold,Revenue\n");

            for(LocalDate date : itemsPerDay.keySet()){
                writer.append(date.toString()).append(",")
                        .append(String.valueOf(itemsPerDay.get(date))).append(",")
                        .append(String.valueOf(revenuePerDay.get(date))).append("\n");
            }

            writer.append("\n");

            // ===== TRANSACTION HISTORY =====
            writer.append("=== TRANSACTION HISTORY ===\n");
            writer.append("Timestamp,Action,Product,Quantity,Total\n");

            for(Transaction t : TransactionManager.transactions){

                writer.append(t.timestamp.toString()).append(",")
                        .append(t.type).append(",")
                        .append(t.product).append(",")
                        .append(String.valueOf(t.quantity)).append(",")
                        .append(String.valueOf(t.total)).append("\n");
            }

            writer.close();

            // ================= SALES DETAILS (NEW FILE) =================
            FileWriter salesWriter = new FileWriter("sales_details.csv");

            salesWriter.append("=== SALES DETAILS ===\n");
            salesWriter.append("Timestamp,Product,Quantity,Total\n");

            for(Transaction t : TransactionManager.transactions){

                if(t.type.equals("SALE")){

                    salesWriter.append(t.timestamp.toString()).append(",")
                            .append(t.product).append(",")
                            .append(String.valueOf(t.quantity)).append(",")
                            .append(String.valueOf(t.total)).append("\n");
                }
            }

            salesWriter.close();

            // ================= ALERT =================
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Report Generated Successfully");
            alert.setContentText("inventory_report.csv and sales_details.csv created.");

            alert.showAndWait();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}