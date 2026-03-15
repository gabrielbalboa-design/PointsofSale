import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleDoubleProperty;
import java.util.Optional;

public class Dashboard {

    BorderPane root;

    TableView<Item> table = new TableView<>();
    ObservableList<Item> inventory = FXCollections.observableArrayList();
    Label totalInventoryValue = new Label();

    VBox sidebar;
    boolean sidebarCollapsed = false;

    VBox productMenuBox;

    public Dashboard(UserRole role){

        root = new BorderPane();

        /* ---------- TABLE + SEARCH ---------- */

        TextField searchField = new TextField();
        searchField.setPromptText("Search product...");

        TableColumn<Item,String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().name));

        TableColumn<Item,String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().supplier));

        supplierCol.setCellFactory(col -> new TableCell<Item,String>() {

            private final Button btn = new Button();

            @Override
            protected void updateItem(String supplier, boolean empty) {

                super.updateItem(supplier, empty);

                if (empty) {
                    setGraphic(null);
                } else {

                    Item item = getTableView().getItems().get(getIndex());

                    btn.setText(supplier);

                    btn.setOnAction(e -> {

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Supplier Information");
                        alert.setHeaderText(item.supplier);
                        alert.setContentText("Contact: " + item.supplierContact);

                        alert.showAndWait();

                    });

                    setGraphic(btn);
                }
            }
        });

        TableColumn<Item,Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().price).asObject());

        TableColumn<Item,Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().quantity).asObject());

        TableColumn<Item,Double> totalCol = new TableColumn<>("Total Value");
        totalCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getTotalValue()).asObject());

        table.getColumns().addAll(nameCol, supplierCol, priceCol, qtyCol, totalCol);

        FilteredList<Item> filteredInventory = new FilteredList<>(inventory, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {

            filteredInventory.setPredicate(item -> {

                if (newVal == null || newVal.isEmpty()) return true;

                return item.name.toLowerCase().contains(newVal.toLowerCase());

            });

        });

        table.setItems(filteredInventory);

        table.setRowFactory(tv -> new TableRow<Item>() {

            @Override
            protected void updateItem(Item item, boolean empty){

                super.updateItem(item, empty);

                if(item == null || empty){
                    setStyle("");
                }
                else if(item.isLowStock()){
                    setStyle("-fx-background-color: #ffcccc;");
                }
                else{
                    setStyle("");
                }
            }
        });

        /* ---------- SIDEBAR TOGGLE BUTTON ---------- */

        Button toggleSidebarBtn = new Button("☰");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar());

        VBox mainContent = new VBox(
                10,
                toggleSidebarBtn,
                searchField,
                table,
                totalInventoryValue
        );

        root.setCenter(mainContent);

        /* ---------- SIDEBAR ---------- */

        sidebar = new VBox(10);
        sidebar.setPrefWidth(180);
        sidebar.setStyle("-fx-padding:10; -fx-background-color:#eeeeee;");

        /* ---------- PRODUCTS SECTION ---------- */

        Button productBtn = new Button("📦 Products");

        productMenuBox = new VBox(5);

        Button addProductBtn = new Button("Add Product");
        Button stockInBtn = new Button("Stock In");
        Button stockOutBtn = new Button("Stock Out");
        Button deleteBtn = new Button("Delete Product");

        productMenuBox.getChildren().addAll(
                addProductBtn,
                stockInBtn,
                stockOutBtn,
                deleteBtn
        );

        productMenuBox.setVisible(false);
        productMenuBox.setManaged(false);

        productBtn.setOnAction(e -> {

            boolean visible = productMenuBox.isVisible();

            productMenuBox.setVisible(!visible);
            productMenuBox.setManaged(!visible);

        });

        /* ---------- ROLE RESTRICTIONS ---------- */

        if(role == UserRole.STAFF){
            deleteBtn.setDisable(true);
        }

        if(role == UserRole.MANAGER){
            deleteBtn.setDisable(true);
        }

        /* ---------- BUTTON ACTIONS ---------- */

        addProductBtn.setOnAction(e -> addProduct());
        stockInBtn.setOnAction(e -> stockIn());
        stockOutBtn.setOnAction(e -> stockOut());
        deleteBtn.setOnAction(e -> deleteProduct());

        /* ---------- REPORT BUTTON ---------- */

        Button reportBtn = new Button("📊 Reports");
        reportBtn.setOnAction(e -> ReportGenerator.generateReport(inventory));

        if(role == UserRole.STAFF){
            reportBtn.setDisable(true);
        }

        sidebar.getChildren().addAll(
                productBtn,
                productMenuBox,
                reportBtn
        );

        /* ---------- ADMIN PANEL ---------- */

        if(role == UserRole.ADMIN){

            Button userBtn = new Button("👥 Users");

            userBtn.setOnAction(e -> {

                VBox adminPanel = UserManagement.createPanel();
                root.setCenter(adminPanel);

            });

            sidebar.getChildren().add(userBtn);

        }

        root.setLeft(sidebar);

        /* ---------- LOAD DATA ---------- */

        BackupLoader.loadBackup(inventory);

        updateTotalValue();

        LowStockAlert.checkLowStock(inventory);
    }

    /* ---------- SIDEBAR COLLAPSE ---------- */

    private void toggleSidebar(){

        if(sidebarCollapsed){

            sidebar.setPrefWidth(180);

        }else{

            sidebar.setPrefWidth(60);

        }

        sidebarCollapsed = !sidebarCollapsed;
    }

    public BorderPane getView(){
        return root;
    }

    /* ---------- PRODUCT FUNCTIONS ---------- */

    private void addProduct(){

        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Add Product");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        TextField nameField = new TextField();
        TextField supplierField = new TextField();
        TextField contactField = new TextField();
        TextField priceField = new TextField();
        TextField qtyField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Supplier:"), 0, 1);
        grid.add(supplierField, 1, 1);

        grid.add(new Label("Contact:"), 0, 2);
        grid.add(contactField, 1, 2);

        grid.add(new Label("Price:"), 0, 3);
        grid.add(priceField, 1, 3);

        grid.add(new Label("Quantity:"), 0, 4);
        grid.add(qtyField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {

            if(dialogButton == addButton){

                try{

                    String name = nameField.getText();
                    String supplier = supplierField.getText();
                    String contact = contactField.getText();

                    double price = Double.parseDouble(priceField.getText());
                    int quantity = Integer.parseInt(qtyField.getText());

                    return new Item(name, supplier, contact, (int) price, quantity);

                }catch(Exception e){
                    showError("Invalid price or quantity.");
                }
            }

            return null;
        });

        Optional<Item> result = dialog.showAndWait();

        result.ifPresent(item -> {
            inventory.add(item);
            updateTotalValue();
            BackupManager.saveBackup(inventory);
        });
    }

    private void stockIn(){

        Item selected = table.getSelectionModel().getSelectedItem();

        if(selected == null){
            showError("Please select a product.");
            return;
        }

        StockEditor.stockIn(selected);

        table.refresh();
        updateTotalValue();
    }

    private void stockOut(){

        Item selected = table.getSelectionModel().getSelectedItem();

        if(selected == null){
            showError("Please select a product.");
            return;
        }

        StockEditor.stockOut(selected);

        table.refresh();
        updateTotalValue();
        BackupManager.saveBackup(inventory);
        LowStockAlert.checkLowStock(inventory);
    }

    private void deleteProduct(){

        Item selected = table.getSelectionModel().getSelectedItem();

        if(selected != null){
            inventory.remove(selected);
            updateTotalValue();
            BackupManager.saveBackup(inventory);
        }else{
            showError("Please select a product.");
        }
    }

    private void updateTotalValue(){

        double total = 0;

        for(Item item : inventory){
            total += item.getTotalValue();
        }

        totalInventoryValue.setText("Total Inventory Value: ₱" + total);
    }

    private void showError(String message){

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
