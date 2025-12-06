package client;

import javax.swing.*;

public class StaffMain {

    static {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "예외 발생: " + e.getMessage());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StaffClient client = new StaffClient();
            try {
                client.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
