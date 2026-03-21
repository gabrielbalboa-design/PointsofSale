import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;

import java.util.ArrayList;
import java.util.Optional;
import javafx.collections.ObservableList;

public class StockEditor {

    public static void stockIn(Item item) {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Stock In");
        dialog.setHeaderText("Enter quantity to add");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            int amount = Integer.parseInt(result.get());

            item.addStock(amount);

            TransactionManager.log("STOCK IN", item.name, amount);
        }
    }

    public static void stockOut(Item item) {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Stock Out");
        dialog.setHeaderText("Enter quantity to remove");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            int amount = Integer.parseInt(result.get());

            if (item.quantity >= amount) {

                item.quantity -= amount;

                TransactionManager.log("STOCK OUT", item.name, amount);

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Not enough stock.");
                alert.showAndWait();
            }
        }
    }

    public static void sellItem(ObservableList<Item> inventory, Item item) {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Sell Item");
        dialog.setHeaderText("Enter quantity to sell");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            int amount = Integer.parseInt(result.get());

            TransactionManager.processSale((ObservableList<Item>) inventory, item.name, amount);
        }
    }
}

