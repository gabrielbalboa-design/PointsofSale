import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.collections.ObservableList;
import java.util.Optional;

public class StockEditor {

    public static void stockIn(Item item){
        item.addStock(1);
        TransactionManager.log("STOCK IN", item.name, 1);
    }

    public static void stockOut(Item item){
        if(item.quantity > 0){
            item.quantity -= 1;
            TransactionManager.log("STOCK OUT", item.name, 1);
        }
    }

    public static void stockInWithInput(Item item){

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Stock In");
        dialog.setHeaderText("Enter quantity to add");

        Optional<String> result = dialog.showAndWait();

        if(result.isPresent()){
            try{
                int amount = Integer.parseInt(result.get());
                item.addStock(amount);
                TransactionManager.log("STOCK IN", item.name, amount);
            }catch(Exception e){
                showError("Invalid input.");
            }
        }
    }

    public static void stockOutWithInput(Item item){

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Stock Out");
        dialog.setHeaderText("Enter quantity to remove");

        Optional<String> result = dialog.showAndWait();

        if(result.isPresent()){
            try{
                int amount = Integer.parseInt(result.get());

                if(item.quantity >= amount){
                    item.quantity -= amount;
                    TransactionManager.log("STOCK OUT", item.name, amount);
                }else{
                    showError("Not enough stock.");
                }

            }catch(Exception e){
                showError("Invalid input.");
            }
        }
    }

    public static void sellItem(ObservableList<Item> inventory, Item item){

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Sell Item");
        dialog.setHeaderText("Enter quantity to sell");

        Optional<String> result = dialog.showAndWait();

        if(result.isPresent()){
            try{
                int amount = Integer.parseInt(result.get());
                TransactionManager.processSale(inventory, item.name, amount);
            }catch(Exception e){
                showError("Invalid input.");
            }
        }
    }

    private static void showError(String msg){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(msg);
        alert.showAndWait();
    }
}