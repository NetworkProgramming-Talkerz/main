/*package server;

import common.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String pcNumber = "UNKNOWN";
    private Server server;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in  = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPcNumber() {
        return pcNumber;
    }

    public void setPcNumber(String pcNumber) {
        this.pcNumber = pcNumber;
    }

    public void send(Message msg) {
        try {
            synchronized (out) {
                out.writeObject(msg);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("전송 실패: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message msg = (Message) in.readObject();
                server.handleMessage(msg, this);
            }
        } catch (Exception e) {
            server.removeClient(this);
        }
    }
}
*/