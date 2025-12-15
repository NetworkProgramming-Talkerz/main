package common;

import java.io.*;
import java.net.*;

public class ClientConnection {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // 연결 수립
    public void connect(String serverIP, int port) throws Exception {
        socket = new Socket(serverIP, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    // 메시지 전송 (동기화 처리 필수)
    public synchronized void sendMessage(Message msg) {
        if (socket == null || socket.isClosed()) return;
        try {
            out.writeObject(msg);
            out.flush();
            out.reset(); // [중요] 객체 캐시 초기화 (같은 객체 재전송 시 변경사항 반영)
        } catch (IOException e) {
            System.err.println("메시지 전송 실패: " + e.getMessage());
        }
    }

    // 메시지 수신 스레드 시작
    public void startReceiver(MessageListener listener) {
        Thread t = new Thread(() -> {
            try {
                while (socket != null && !socket.isClosed()) {
                    Message msg = (Message) in.readObject();
                    listener.onMessage(msg);
                }
            } catch (SocketException se) {
                System.out.println("서버 연결이 종료되었습니다.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close();
            }
        });
        t.setDaemon(true); // 메인 스레드 종료 시 함께 종료
        t.start();
    }

    // 리소스 정리
    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
    }

    // 콜백 인터페이스
    public interface MessageListener {
        void onMessage(Message msg);
    }
}