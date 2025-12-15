package client;

import javax.swing.SwingUtilities;

public class CustomerMain {
    public static void main(String[] args) {
        // Swing UI는 반드시 EDT(Event Dispatch Thread)에서 생성해야 안전함
        SwingUtilities.invokeLater(() -> {
            try {
                CustomerClient client = new CustomerClient();
                // start() 내부의 네트워크 연결 등은 별도 스레드에서 돌리는 것이 정석이나,
                // 간단한 학습용 프로젝트에서는 여기서 호출해도 무방함 (단, 연결 중 화면이 멈출 수 있음)
                client.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}