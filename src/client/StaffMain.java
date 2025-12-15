package client;

import javax.swing.*;

public class StaffMain {

    static {
        // 프로그램 전역에서 발생하는 예기치 못한 에러를 팝업으로 표시
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "시스템 오류 발생: " + e.getMessage());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                StaffClient client = new StaffClient();
                client.start();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "클라이언트 시작 실패: " + e.getMessage());
            }
        });
    }
}