import javafx.collections.*;
import java.io.*;
import java.util.*;

public class SupplierManager {

    static final String SUPPLIER_FILE = "supplier_information.csv";

    public static ObservableList<String> supplierNames = FXCollections.observableArrayList();
    public static ObservableList<String> supplierContacts = FXCollections.observableArrayList();

    static {
        loadSuppliers();
    }

    public static ObservableList<String> getSuppliers() {
        return supplierNames;
    }

    public static void addSupplier(String name, String contact){
        if(name == null || name.isEmpty()) return;

        if(!supplierNames.contains(name)){
            supplierNames.add(name);
            supplierContacts.add(contact);
            saveSuppliers();
        }
    }

    public static String getContact(String supplierName){
        int index = supplierNames.indexOf(supplierName);
        if(index >= 0){
            return supplierContacts.get(index);
        }
        return "";
    }

    public static void saveSuppliers(){
        try(PrintWriter writer = new PrintWriter(new FileWriter(SUPPLIER_FILE))){

            writer.println("SupplierName,Contact");

            for(int i = 0; i < supplierNames.size(); i++){
                writer.println(supplierNames.get(i) + "," + supplierContacts.get(i));
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void loadSuppliers(){
        File file = new File(SUPPLIER_FILE);

        if(!file.exists()) return;

        try(BufferedReader reader = new BufferedReader(new FileReader(SUPPLIER_FILE))){

            String line = reader.readLine();

            while((line = reader.readLine()) != null){
                String[] parts = line.split(",", 2);
                if(parts.length >= 2){
                    String name = parts[0];
                    String contact = parts[1];

                    if(!supplierNames.contains(name)){
                        supplierNames.add(name);
                        supplierContacts.add(contact);
                    }
                }
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}