package common;

import java.io.Serializable;

public class TimeInfo implements Serializable {
    // 직렬화 버전 ID (클래스 변경 시 충돌 방지)
    private static final long serialVersionUID = 1L;

    private int remainingMinutes;

    public TimeInfo() {
        this(0);
    }

    public TimeInfo(int minutes) {
        this.remainingMinutes = minutes;
    }

    public int getRemainingMinutes() {
        return remainingMinutes;
    }

    public void setRemainingMinutes(int minutes) {
        this.remainingMinutes = minutes;
    }
}