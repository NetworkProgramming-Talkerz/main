package common;
import java.io.*;
import java.net.*;


public class ClientConnection {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;


    public void connect(String serverIP, int port) throws Exception {
        socket = new Socket(serverIP, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }


    public void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void startReceiver(MessageListener listener) {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    Message msg = (Message) in.readObject();
                    listener.onMessage(msg);
                }
            } catch (Exception e) {
                System.out.println("수신 스레드 종료: " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
    }

    public interface MessageListener { void onMessage(Message msg); }
}