public class Item {

    String name;
    String supplier;
    String supplierContact;

    int quantity;
    double price;

    int minStock = 10;

    public Item(String name, String supplier, String supplierContact, int quantity, double price){

        this.name = name;
        this.supplier = supplier;
        this.supplierContact = supplierContact;
        this.quantity = quantity;
        this.price = price;

    }

    public boolean isLowStock(){
        return quantity <= minStock;
    }

    public double getTotalValue(){
        return quantity * price;
    }

}