// (imports unchanged)

import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.stage.Stage;
import java.util.Optional;

public class Dashboard {

    BorderPane root;

    TableView<Item> table = new TableView<>();
    ObservableList<Item> inventory = FXCollections.observableArrayList();
    Label totalInventoryValue = new Label();

    VBox sidebar;
    boolean sidebarCollapsed = false;

    VBox productMenuBox;
    VBox mainContent;

    public Dashboard(UserRole role){

        root = new BorderPane();

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

        TableColumn<Item, Void> actionCol = new TableColumn<>("Actions");

        actionCol.setCellFactory(col -> new TableCell<>() {

            private final Button plus = new Button("+");
            private final Button minus = new Button("-");
            private final HBox box = new HBox(5, plus, minus);

            {
                plus.setOnAction(e -> {
                    Item item = getTableView().getItems().get(getIndex());

                    StockEditor.stockIn(item);

                    table.refresh();
                    updateTotalValue();
                    BackupManager.saveBackup(inventory);
                });

                minus.setOnAction(e -> {
                    Item item = getTableView().getItems().get(getIndex());

                    StockEditor.stockOut(item);

                    table.refresh();
                    updateTotalValue();
                    BackupManager.saveBackup(inventory);
                    LowStockAlert.checkLowStock(inventory);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty){
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(nameCol, supplierCol, priceCol, qtyCol, totalCol, actionCol);

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

        Button toggleSidebarBtn = new Button("☰");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar());

        mainContent = new VBox(10, toggleSidebarBtn, searchField, table, totalInventoryValue);
        root.setCenter(mainContent);

        sidebar = new VBox(10);
        sidebar.setPrefWidth(180);
        sidebar.setStyle("-fx-padding:10; -fx-background-color:#eeeeee;");

        Button productBtn = new Button("📦 Products");

        productMenuBox = new VBox(5);

        Button addProductBtn = new Button("Add Product");
        Button stockInBtn = new Button("Stock In");
        Button sellBtn = new Button("Sell"); // 🔥 MULTI SELL
        Button stockOutBtn = new Button("Stock Out");
        Button deleteBtn = new Button("Delete Product");

        productMenuBox.getChildren().addAll(
                addProductBtn,
                stockInBtn,
                sellBtn,
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

        if(role == UserRole.STAFF || role == UserRole.MANAGER){
            deleteBtn.setDisable(true);
        }

        addProductBtn.setOnAction(e -> addProduct());
        stockInBtn.setOnAction(e -> stockIn());
        sellBtn.setOnAction(e -> sellItem()); // 🔥 MULTI SELL
        stockOutBtn.setOnAction(e -> stockOut());
        deleteBtn.setOnAction(e -> deleteProduct());

        Button reportBtn = new Button("📊 Reports");
        reportBtn.setOnAction(e -> ReportGenerator.generateReport(inventory));

        if(role == UserRole.STAFF){
            reportBtn.setDisable(true);
        }

        Button logoutBtn = new Button("🚪 Logout");

        logoutBtn.setOnAction(e -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(LoginScreen.createLoginScene(stage));
        });

        sidebar.getChildren().addAll(productBtn, productMenuBox, reportBtn, logoutBtn);

        if(role == UserRole.ADMIN){

            Button userBtn = new Button("👥 Users");

            userBtn.setOnAction(e -> {
                VBox adminPanel = UserManagement.createPanel();

                Button backBtn = new Button("⬅ Back to Dashboard");
                backBtn.setOnAction(ev -> root.setCenter(mainContent));

                VBox wrapper = new VBox(10, backBtn, adminPanel);
                root.setCenter(wrapper);
            });

            sidebar.getChildren().add(userBtn);
        }

        root.setLeft(sidebar);

        BackupLoader.loadBackup(inventory);

        updateTotalValue();
        LowStockAlert.checkLowStock(inventory);
    }

    private void toggleSidebar(){
        sidebar.setPrefWidth(sidebarCollapsed ? 180 : 60);
        sidebarCollapsed = !sidebarCollapsed;
    }

    public BorderPane getView(){
        return root;
    }

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
                    return new Item(
                            nameField.getText(),
                            supplierField.getText(),
                            contactField.getText(),
                            Integer.parseInt(qtyField.getText()),
                            Double.parseDouble(priceField.getText())
                    );
                }catch(Exception e){
                    showError("Invalid price or quantity.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(item -> {
            inventory.add(item);
            updateTotalValue();
            BackupManager.saveBackup(inventory);
        });
    }

    private void stockIn(){
        Item selected = table.getSelectionModel().getSelectedItem();
        if(selected == null){ showError("Please select a product."); return; }
        StockEditor.stockIn(selected);
        table.refresh();
        updateTotalValue();
    }

    private void stockOut(){
        Item selected = table.getSelectionModel().getSelectedItem();
        if(selected == null){ showError("Please select a product."); return; }
        StockEditor.stockOut(selected);
        table.refresh();
        updateTotalValue();
        BackupManager.saveBackup(inventory);
        LowStockAlert.checkLowStock(inventory);
    }

    private void sellItem(){

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Process Sale");

        VBox content = new VBox(10);

        // 🔽 Supplier dropdown
        ComboBox<String> supplierBox = new ComboBox<>();

        // 🔽 Item dropdown
        ComboBox<Item> itemBox = new ComboBox<>();

        // 🔢 Quantity input
        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");

        // 🛒 Cart display
        TextArea cartArea = new TextArea();
        cartArea.setEditable(false);

        // store cart
        java.util.Map<Item, Integer> cart = new java.util.HashMap<>();

        // 🔥 Fill suppliers
        for(Item item : inventory){
            if(!supplierBox.getItems().contains(item.supplier)){
                supplierBox.getItems().add(item.supplier);
            }
        }

        // 🔄 When supplier selected → filter items
        supplierBox.setOnAction(e -> {
            itemBox.getItems().clear();

            for(Item item : inventory){
                if(item.supplier.equals(supplierBox.getValue())){
                    itemBox.getItems().add(item);
                }
            }
        });

        // 🔥 Display item names nicely
        itemBox.setCellFactory(lv -> new ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty){
                super.updateItem(item, empty);
                setText(empty ? "" : item.name + " (Stock: " + item.quantity + ")");
            }
        });

        itemBox.setButtonCell(new ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty){
                super.updateItem(item, empty);
                setText(empty ? "" : item.name);
            }
        });

        // ➕ Add to cart button
        Button addBtn = new Button("➕ Add Item");

        addBtn.setOnAction(e -> {

            Item selectedItem = itemBox.getValue();

            if(selectedItem == null){
                showError("Select an item.");
                return;
            }

            try{
                int qty = Integer.parseInt(qtyField.getText());

                if(qty <= 0){
                    showError("Invalid quantity.");
                    return;
                }

                cart.put(selectedItem, cart.getOrDefault(selectedItem, 0) + qty);

                // update cart display
                cartArea.clear();

                for(Item i : cart.keySet()){
                    cartArea.appendText(i.name + " x" + cart.get(i) + "\n");
                }

                qtyField.clear();

            }catch(Exception ex){
                showError("Enter valid number.");
            }
        });

        content.getChildren().addAll(
                new Label("Supplier:"), supplierBox,
                new Label("Item:"), itemBox,
                new Label("Quantity:"), qtyField,
                addBtn,
                new Label("Cart:"), cartArea
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {

            if(btn == ButtonType.OK){

                double totalBill = 0;

                for(Item item : cart.keySet()){

                    int qty = cart.get(item);

                    TransactionManager.processSale(item, qty);
                    totalBill += item.price * qty;
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Sale Completed");
                alert.setContentText("Total Bill: ₱" + totalBill);
                alert.showAndWait();

                table.refresh();
                updateTotalValue();
                BackupManager.saveBackup(inventory);
                LowStockAlert.checkLowStock(inventory);
            }

            return null;
        });

        dialog.showAndWait();
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