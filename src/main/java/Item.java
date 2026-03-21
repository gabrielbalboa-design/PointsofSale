public class Item {

    String name;
    String supplier;
    String supplierContact;

    int quantity;
    double price;

    int minStock = 10;

    int soldCount = 0;

    public Item(String name, String supplier, String supplierContact, int quantity, double price){

        this.name = name;
        this.supplier = supplier;
        this.supplierContact = supplierContact;
        this.quantity = quantity;
        this.price = price;

    }
    public int getQuantity(){
        return quantity;
    }
    public boolean isLowStock(){
        return quantity <= minStock;
    }

    public double getTotalValue(){
        return quantity * price;
    }

    public boolean reduceStock(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            soldCount += amount;
            return true;
        }
        return false;
    }

    public void addStock(int amount) {
        quantity += amount;
    }

}