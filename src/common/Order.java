package common;
import java.io.Serializable;


public class Order implements Serializable {
    public String menuName;
    public String pcNumber;
    private int totalPrice;
    public long timestamp;


    public Order(String menuName, String pcNumber, int totalPrice, long timestamp) {
        this.menuName = menuName;
        this.pcNumber = pcNumber;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
    }

    public String getMenuName() { return menuName; }
    public String getPcNumber() { return pcNumber; }
    public int getTotalPrice() {
        return totalPrice;
    }
    public long getTimestamp() { return timestamp; }
}