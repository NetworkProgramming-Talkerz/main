package server;

import javax.swing.SwingUtilities;

public class ServerMain {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            Server server = new Server();
            server.start(5000);
        });

        System.out.println(">>> ServerMain STARTED");
    }
}
