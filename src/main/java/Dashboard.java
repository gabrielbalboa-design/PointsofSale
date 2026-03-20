import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Dashboard {

    BorderPane root;

    TableView<Item> table = new TableView<>();
    TableView<Transaction> transactionTable = new TableView<>();

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

            private Timeline plusTimer;
            private Timeline minusTimer;

            {
                plus.setOnMousePressed(e -> {
                    Item item = getTableView().getItems().get(getIndex());

                    item.addStock(1);
                    table.refresh();
                    updateTotalValue();

                    plusTimer = new Timeline(new KeyFrame(Duration.millis(80), ev -> {
                        item.addStock(1);
                        table.refresh();
                        updateTotalValue();
                    }));

                    plusTimer.setCycleCount(Timeline.INDEFINITE);
                    plusTimer.play();
                });

                plus.setOnMouseReleased(e -> stopPlus());
                plus.setOnMouseExited(e -> stopPlus());

                minus.setOnMousePressed(e -> {
                    Item item = getTableView().getItems().get(getIndex());

                    if(item.quantity > 0){
                        item.quantity -= 1;
                    }

                    table.refresh();
                    updateTotalValue();

                    minusTimer = new Timeline(new KeyFrame(Duration.millis(80), ev -> {
                        if(item.quantity > 0){
                            item.quantity -= 1;
                        }
                        table.refresh();
                        updateTotalValue();
                    }));

                    minusTimer.setCycleCount(Timeline.INDEFINITE);
                    minusTimer.play();
                });

                minus.setOnMouseReleased(e -> stopMinus());
                minus.setOnMouseExited(e -> stopMinus());
            }

            private void stopPlus(){
                if(plusTimer != null) plusTimer.stop();
            }

            private void stopMinus(){
                if(minusTimer != null) minusTimer.stop();
            }

            @Override
            protected void updateItem(Void item, boolean empty){
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(nameCol, supplierCol, priceCol, qtyCol, totalCol, actionCol);

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(table, Priority.ALWAYS);

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

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().type));

        TableColumn<Transaction, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().product));

        TableColumn<Transaction, Integer> amountCol = new TableColumn<>("Qty");
        amountCol.setCellValueFactory(data -> {
            return new javafx.beans.property.SimpleIntegerProperty(data.getValue().amount).asObject();
        });

        TableColumn<Transaction, Double> totalSaleCol = new TableColumn<>("Total");
        totalSaleCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().total).asObject());

        transactionTable.getColumns().addAll(typeCol, productCol, amountCol, totalSaleCol);
        transactionTable.setItems(TransactionManager.transactions);
        transactionTable.setPrefHeight(150);

        Button toggleSidebarBtn = new Button("☰");
        toggleSidebarBtn.setOnAction(e -> toggleSidebar());

        mainContent = new VBox(
                10,
                toggleSidebarBtn,
                searchField,
                table,
                new Label("Transactions"),
                transactionTable,
                totalInventoryValue
        );

        root.setCenter(mainContent);

        sidebar = new VBox(10);
        sidebar.setPrefWidth(180);
        sidebar.setStyle("-fx-padding:10; -fx-background-color:#eeeeee;");

        Button productBtn = new Button("📦 Products");

        productMenuBox = new VBox(5);

        Button addProductBtn = new Button("Add Product");
        Button stockInBtn = new Button("Stock In");
        Button sellBtn = new Button("Sell");
        Button stockOutBtn = new Button("Stock Out");
        Button deleteBtn = new Button("Delete Product");

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
                StockEditor.stockInWithInput(selected);
                table.refresh();
                updateTotalValue();
            } else {
                showError("Please select a product.");
            }
        });

        stockOutBtn.setOnAction(e -> {
            Item selected = table.getSelectionModel().getSelectedItem();
            if(selected != null){
                StockEditor.stockOutWithInput(selected);
                table.refresh();
                updateTotalValue();
            } else {
                showError("Please select a product.");
            }
        });

        sellBtn.setOnAction(e -> sellItem());
        deleteBtn.setOnAction(e -> deleteProduct());

        Button reportBtn = new Button("📊 Reports");
        reportBtn.setOnAction(e -> ReportGenerator.generateReport(inventory));

        Button logoutBtn = new Button("🚪 Logout");

        logoutBtn.setOnAction(e -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(LoginScreen.createLoginScene(stage));
        });

        sidebar.getChildren().addAll(productBtn, productMenuBox, reportBtn, logoutBtn);

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
                    showError("Invalid input.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(item -> {
            inventory.add(item);
            updateTotalValue();
        });
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