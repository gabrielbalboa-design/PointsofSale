import javafx.collections.*;

public class SupplierManager {

    public static ObservableList<String> supplierNames = FXCollections.observableArrayList();
    public static ObservableList<String> supplierContacts = FXCollections.observableArrayList();

    public static void addSupplier(String name, String contact){
        supplierNames.add(name);
        supplierContacts.add(contact);
    }

    public static String getContact(String supplierName){
        int index = supplierNames.indexOf(supplierName);
        if(index >= 0){
            return supplierContacts.get(index);
        }
        return "";
    }
}