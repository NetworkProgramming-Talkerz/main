package common;
import java.io.Serializable;


public class TimeInfo implements Serializable {
    public int remainingMinutes;
    public TimeInfo(int minutes) { this.remainingMinutes = minutes; }

    public int getRemainingMinutes() {
        return remainingMinutes;
    }
}