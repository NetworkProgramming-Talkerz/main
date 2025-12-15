package server;

import javax.swing.SwingUtilities;

public class ServerMain {

    private static final int PORT = 5000;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Server server = new Server();
            server.start(PORT);
        });

        // 주의: invokeLater는 비동기이므로 이 로그가 서버 시작보다 먼저 찍힐 수 있음
        System.out.println(">>> 서버 초기화 중... (Port: " + PORT + ")");
    }
}