import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.collections.ObservableList;

public class LowStockAlert {

    private static boolean alreadyShown = false;

    public static void checkLowStock(ObservableList<Item> inventory){

        StringBuilder lowStockList = new StringBuilder();

        for(Item item : inventory){

            if(item.isLowStock()){

                lowStockList.append("Product: ").append(item.name)
                        .append("\nQuantity: ").append(item.quantity).append(" left")
                        .append("\nSupplier: ").append(item.supplier)
                        .append("\nContact: ").append(item.supplierContact)
                        .append("\n\n");
            }
        }

        if(lowStockList.length() > 0 && !alreadyShown){

            Alert alert = new Alert(Alert.AlertType.WARNING);

            alert.setTitle("Low Stock Warning");
            alert.setHeaderText("⚠ The following items are running low:");

            TextArea area = new TextArea(lowStockList.toString());
            area.setEditable(false);
            area.setWrapText(true);

            alert.getDialogPane().setContent(area);

            alert.showAndWait();

            alreadyShown = true;
        }

        if(lowStockList.length() == 0){
            alreadyShown = false;
        }
    }
}