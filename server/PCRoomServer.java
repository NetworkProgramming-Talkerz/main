package server;

import common.ChatMsg;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class PCRoomServer {
    private ServerSocket serverSocket;
    private int port = 54321;
    private Vector<ClientHandler> users = new Vector<>(); // 접속자 목록 (손님+관리자)
    private PCRoomServerGUI gui; // 로그 출력을 위한 GUI 참조

    public PCRoomServer(int port, PCRoomServerGUI gui) {
        this.port = port;
        this.gui = gui;
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            log("✅ 서버 소켓 시작 (Port: " + port + ")");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                log("New Connection: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                users.add(handler); // Vector에 추가
                handler.start();
            }
        } catch (Exception e) {
            log("❌ 서버 에러: " + e.getMessage());
        }
    }

    // 로그 출력 헬퍼 메서드
    public void log(String msg) {
        if (gui != null) gui.appendLog(msg);
        System.out.println(msg);
    }

    // ---------------- [라우팅 로직] ----------------
    // 1. 전체 공지 (관리자 -> 모두)
    public void broadcast(ChatMsg msg) {
        for (ClientHandler user : users) {
            user.send(msg);
        }
    }

    // 2. 관리자에게만 전송 (손님 -> 관리자)
    public void sendToAdmin(ChatMsg msg) {
        for (ClientHandler user : users) {
            if (user.isAdmin) user.send(msg);
        }
    }

    // 3. 1:1 전송 (관리자 <-> 특정 손님)
    public void sendToUser(String targetID, ChatMsg msg) {
        for (ClientHandler user : users) {
            if (user.uid != null && user.uid.equals(targetID)) {
                user.send(msg);
                return;
            }
        }
        log("❌ 전송 실패: [" + targetID + "] 없음");
    }

    // ---------------- [내부 클래스: 핸들러] ----------------
    class ClientHandler extends Thread {
        Socket socket;
        ObjectInputStream in;
        ObjectOutputStream out;
        String uid;
        boolean isAdmin = false; // 관리자 여부

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (Exception e) { e.printStackTrace(); }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    ChatMsg msg = (ChatMsg) in.readObject();

                    switch (msg.mode) {
                        case ChatMsg.MODE_LOGIN:
                            this.uid = msg.userID;
                            this.isAdmin = false;
                            log("💻 [손님 입장] " + uid);
                            // 접속 시, 현재 접속자들에게 알림 (선택 사항)
                            // broadcast(new ChatMsg("SERVER", ChatMsg.MODE_TX_STRING, uid + "님 입장"));
                            break;

                        case ChatMsg.MODE_ADMIN_LOGIN:
                            this.uid = msg.userID;
                            this.isAdmin = true;
                            log("👮 [관리자 로그인] " + uid);
                            break;

                        case ChatMsg.MODE_TX_STRING: // 일반 채팅
                            if (this.isAdmin) {
                                // 관리자가 말하면 -> 전체 공지
                                broadcast(msg);
                            } else {
                                // 손님이 말하면 -> 관리자에게만
                                sendToAdmin(msg);
                            }
                            break;

                        case ChatMsg.MODE_PRIVATE_CHAT: // 1:1 대화
                            // 받는 사람(receiver)에게만 전달
                            sendToUser(msg.receiver, msg);
                            break;

                        case ChatMsg.MODE_TX_ORDER: // 주문
                            log("🍔 [주문] " + uid + ": " + msg.message);
                            sendToAdmin(msg);
                            break;

                        case ChatMsg.MODE_LOGOUT:
                            users.remove(this);
                            log(uid + " 퇴장. 현재 인원: " + users.size());
                            return;
                    }
                }
            } catch (Exception e) {
                users.remove(this);
                log(uid + " 비정상 종료.");
            }
        }

        public void send(ChatMsg msg) {
            try {
                out.writeObject(msg);
                out.flush();
                out.reset();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}