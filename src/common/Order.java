package common;
import java.io.Serializable;


public class Order implements Serializable {
    public String menuName;
    public String pcNumber;
    public long timestamp;


    public Order(String menuName, String pcNumber, long timestamp) {
        this.menuName = menuName;
        this.pcNumber = pcNumber;
        this.timestamp = timestamp;
    }

    public String getMenuName() { return menuName; }
    public String getPcNumber() { return pcNumber; }
    public long getTimestamp() { return timestamp; }
}