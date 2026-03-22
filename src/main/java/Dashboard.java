import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.stage.Stage;
import java.util.Optional;

public class Dashboard {
    ObservableList<Transaction> salesData = FXCollections.observableArrayList();
    BorderPane root;
    TableView<Transaction> salesTable = new TableView<>();
    TableView<Item> table = new TableView<>();
    ObservableList<Item> inventory = FXCollections.observableArrayList();
    Label totalInventoryValue = new Label();

    VBox sidebar;
    boolean sidebarCollapsed = false;

    VBox productMenuBox;
    VBox mainContent;

    public Dashboard(UserRole role) {

        root = new BorderPane();

        TextField searchField = new TextField();
        searchField.setPromptText("Search product...");

        TableColumn<Item, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().name));

        TableColumn<Item, String> supplierCol = new TableColumn<>("Supplier");
        supplierCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().supplier));
        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().type));

        TableColumn<Transaction, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().product));

        TableColumn<Transaction, Integer> qtyCol2 = new TableColumn<>("Qty");
        qtyCol2.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().quantity).asObject());

        TableColumn<Transaction, Double> totalCol2 = new TableColumn<>("Total");
        totalCol2.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().total).asObject());

        salesTable.getColumns().addAll(typeCol, productCol, qtyCol2, totalCol2);
        salesData.addAll(TransactionManager.transactions);
        salesTable.setItems(salesData);
        supplierCol.setCellFactory(col -> new TableCell<Item, String>() {

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

        TableColumn<Item, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().price).asObject());

        TableColumn<Item, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().quantity).asObject());

        TableColumn<Item, Double> totalCol = new TableColumn<>("Total Value");
        totalCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getTotalValue()).asObject());

        TableColumn<Item, Void> actionCol = new TableColumn<>("Stock");

        actionCol.setCellFactory(col -> new TableCell<>() {

            private final Button plus = new Button("+");
            private final Button minus = new Button("-");
            private final HBox box = new HBox(5, plus, minus);
            {

                plus.setOnAction(e -> {
                    Item item = getTableView().getItems().get(getIndex());

                    item.addStock(1);

                    table.refresh();
                    updateTotalValue();
                    BackupManager.saveBackup(inventory);;
                });

                minus.setOnAction(e -> {
                    Item item = getTableView().getItems().get(getIndex());

                    if(item.getQuantity() > 0){
                        item.reduceStock(1);
                    }

                    table.refresh();
                    updateTotalValue();
                    BackupManager.saveBackup(inventory);;
                });
            }


            @Override
            protected void updateItem(Void item, boolean empty) {
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
        toggleSidebarBtn.setOnAction(e -> toString());

        Label salesLabel = new Label("Sales Transactions");

        mainContent = new VBox(10,
                toggleSidebarBtn,
                searchField,
                table,
                totalInventoryValue,
                salesLabel,
                salesTable
        );
        root.setCenter(mainContent);

        sidebar = new VBox(10);
        sidebar.setPrefWidth(180);
        sidebar.setStyle("-fx-padding:10; -fx-background-color:#eeeeee;");

        Button productBtn = new Button("📦 Products");
        Button supplierBtn = new Button("🏭 Suppliers");

        supplierBtn.setDisable(role == UserRole.STAFF);

        supplierBtn.setOnAction(e -> {

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Add Supplier");

            TextField nameField = new TextField();
            TextField contactField = new TextField();

            VBox box = new VBox(10,
                    new Label("Supplier Name"), nameField,
                    new Label("Contact"), contactField
            );

            dialog.getDialogPane().setContent(box);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(btn -> {
                if(btn == ButtonType.OK){
                    SupplierManager.addSupplier(nameField.getText(), contactField.getText());
                }
                return null;
            });

            dialog.showAndWait();
        });

        productMenuBox = new VBox(5);

        Button addProductBtn = new Button("Add Product");
        Button stockInBtn = new Button("Stock In");
        Button sellBtn = new Button("\uD83D\uDCB0Sell");
        Button stockOutBtn = new Button("Stock Out");
        Button deleteBtn = new Button("Delete Product");

        boolean isAdmin = role == UserRole.ADMIN;
        boolean isManager = role == UserRole.MANAGER;

        boolean isStaff = role == UserRole.STAFF;

        addProductBtn.setDisable(isStaff);
        deleteBtn.setDisable(isStaff);

        stockInBtn.setDisable(false);
        stockOutBtn.setDisable(false);
        sellBtn.setDisable(false);

        Button reportBtn = new Button("📊 Reports");
        reportBtn.setDisable(isStaff);
        reportBtn.setOnAction(e -> ReportGenerator.generateReport(inventory));

        productMenuBox.getChildren().addAll(
                addProductBtn, stockInBtn, sellBtn, stockOutBtn, deleteBtn
        );

        productMenuBox.setVisible(false);
        productMenuBox.setManaged(false);

        productBtn.setOnAction(e -> {
            boolean visible = productMenuBox.isVisible();
            productMenuBox.setVisible(!visible);
            productMenuBox.setManaged(!visible);
        });

        addProductBtn.setOnAction(e -> addProduct());

        stockInBtn.setOnAction(e -> {
            Item selected = table.getSelectionModel().getSelectedItem();
            if(selected != null){
                StockEditor.stockIn(selected);
                BackupManager.saveBackup(inventory);
                table.refresh();
                updateTotalValue();
                LowStockAlert.checkLowStock(inventory);
            } else {
                showError("Please select a product.");
            }
        });

        stockOutBtn.setOnAction(e -> {
            Item selected = table.getSelectionModel().getSelectedItem();
            if(selected != null){
                StockEditor.stockOut(selected);
                BackupManager.saveBackup(inventory);
                table.refresh();
                updateTotalValue();
                LowStockAlert.checkLowStock(inventory);
            } else {
                showError("Please select a product.");
            }
        });

        sellBtn.setOnAction(e -> sellItem());
        deleteBtn.setOnAction(e -> deleteProduct());


        Button logoutBtn = new Button("🚪 Logout");

        logoutBtn.setOnAction(e -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(LoginScreen.createLoginScene(stage));
        });

        if(role == UserRole.ADMIN){

            Button userBtn = new Button("👥 Users");

            userBtn.setOnAction(e -> {

                TableView<User> userTable = new TableView<>();

                TableColumn<User, String> usernameCol = new TableColumn<>("Username");
                usernameCol.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().username));

                TableColumn<User, String> passwordCol = new TableColumn<>("Password");
                passwordCol.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().password));

                passwordCol.setCellFactory(col -> new TableCell<User, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty){
                        super.updateItem(item, empty);

                        if(empty){
                            setText(null);
                            return;
                        }

                        User user = getTableView().getItems().get(getIndex());

                        if(user.firstLogin){
                            setText(user.originalPassword);
                        }else{
                            setText("*****");
                        }
                    }
                });

                TableColumn<User, String> roleCol = new TableColumn<>("Role");
                roleCol.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().role.toString()));

                userTable.getColumns().addAll(usernameCol, passwordCol, roleCol);
                userTable.setItems(FXCollections.observableArrayList(UserManager.users));

                VBox adminPanel = UserManagement.createPanel();
                Button deleteUserBtn = new Button("Delete User");

                deleteUserBtn.setOnAction(ev -> {

                    User selected = userTable.getSelectionModel().getSelectedItem();

                    if(selected == null){
                        showError("Select a user.");
                        return;
                    }

                    if(selected.role == UserRole.ADMIN){
                        showError("Cannot delete admin.");
                        return;
                    }

                    userTable.getItems().remove(selected);
                    UserManager.saveUsers();
                });
                Button backBtn = new Button("⬅ Back to Dashboard");
                backBtn.setOnAction(ev -> root.setCenter(mainContent));

                VBox layout = new VBox(10, backBtn, adminPanel, new Label("Users"), userTable);

                root.setCenter(layout);
            });

            sidebar.getChildren().add(userBtn);
        }
        sidebar.getChildren().addAll(productBtn, productMenuBox, sellBtn, logoutBtn,reportBtn);
        sidebar.getChildren().add(supplierBtn);

        root.setLeft(sidebar);

        BackupLoader.loadBackup(inventory);

        updateTotalValue();
        LowStockAlert.checkLowStock(inventory);
    }
    private void addProduct(){
        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Add Product");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        TextField nameField = new TextField();
        ComboBox<String> supplierField = new ComboBox<>();
        supplierField.setItems(SupplierManager.getSuppliers());
        TextField priceField = new TextField();
        TextField qtyField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Price:"), 0, 3);
        grid.add(priceField, 1, 3);
        grid.add(new Label("Supplier:"), 0, 1);
        grid.add(supplierField, 1, 1);
        grid.add(new Label("Quantity:"), 0, 4);
        grid.add(qtyField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if(dialogButton == addButton){
                try{
                    return new Item(
                            nameField.getText(),
                            supplierField.getValue(),
                            SupplierManager.getContact(supplierField.getValue()),
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

        ComboBox<String> supplierBox = new ComboBox<>();

        ComboBox<Item> itemBox = new ComboBox<>();

        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");

        TextArea cartArea = new TextArea();
        cartArea.setEditable(false);

        java.util.Map<Item, Integer> cart = new java.util.HashMap<>();

        for(Item item : inventory){
            if(!supplierBox.getItems().contains(item.supplier)){
                supplierBox.getItems().add(item.supplier);
            }
        }

        supplierBox.setOnAction(e -> {
            itemBox.getItems().clear();

            for(Item item : inventory){
                if(item.supplier.equals(supplierBox.getValue())){
                    itemBox.getItems().add(item);
                }
            }
        });

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

                    boolean success = TransactionManager.processSale(item, qty);

                    if(success){
                        totalBill += item.price * qty;
                    } else {
                        showError("Not enough stock for " + item.name);
                    }
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Sale Completed");
                alert.setContentText("Total Bill: ₱" + totalBill);
                alert.showAndWait();

                table.refresh();
                updateTotalValue();
                salesData.clear();
                salesData.addAll(TransactionManager.transactions);
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
    public BorderPane getView(){
        return root;
    }
}
