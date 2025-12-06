package server;

import common.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List; // java.awt.List와 충돌 방지
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server extends JFrame {

    private ServerSocket serverSocket;
    private List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private Map<String, Integer> timeMap = Collections.synchronizedMap(new HashMap<>());

    private JTextArea logArea = new JTextArea();
    private JTextArea orderArea = new JTextArea();
    private JCheckBox chkChat = new JCheckBox("CHAT/PRIVATE/GROUP", true);
    private JCheckBox chkOrder = new JCheckBox("ORDER", true);
    private JCheckBox chkTime = new JCheckBox("TIME_UPDATE", true);
    private JCheckBox chkEmoji = new JCheckBox("EMOJI", true);

    private ScheduledExecutorService scheduler;

    public Server() {
        setTitle("PC방 서버 (직원용 중계 서버)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        buildUI();
        startTimeScheduler();
    }

    private void buildUI() {
        logArea.setEditable(false);
        orderArea.setEditable(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(chkChat);
        filterPanel.add(chkOrder);
        filterPanel.add(chkTime);
        filterPanel.add(chkEmoji);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(logArea),
                new JScrollPane(orderArea)
        );
        split.setDividerLocation(500);

        orderArea.setBorder(BorderFactory.createTitledBorder("주문 내역"));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(filterPanel, BorderLayout.NORTH);
        getContentPane().add(split, BorderLayout.CENTER);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            appendLog("서버 시작: port=" + port);

            Thread acceptThread = new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket socket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(socket, this);
                        clients.add(handler);
                        handler.start();
                    } catch (IOException e) {
                        appendLog("accept 에러: " + e.getMessage());
                    }
                }
            });
            acceptThread.setDaemon(true);
            acceptThread.start();

            setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버 시작 실패: " + e.getMessage());
        }
    }

    private void startTimeScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (timeMap) {
                for (Map.Entry<String, Integer> entry : timeMap.entrySet()) {
                    String pc = entry.getKey();
                    int t = entry.getValue();
                    if (t > 0) {
                        t -= 1;
                        entry.setValue(t);
                        // 시간 업데이트는 로그에 너무 많이 찍히므로 로그 제외
                        Message timeMsg = new Message(
                                Message.Mode.TIME_UPDATE, "SERVER", pc, "시간", new TimeInfo(t)
                        );
                        sendTo(pc, timeMsg);
                    }
                }
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    public void handleMessage(Message msg, ClientHandler sender) {
        if (msg.getMode() == Message.Mode.CONNECT) {
            String pc = msg.getSender();
            sender.setPcNumber(pc);
            synchronized (timeMap) {
                timeMap.putIfAbsent(pc, 120);
            }
        }

        appendLogFiltered(msg);

        switch (msg.getMode()) {
            case CONNECT:
                broadcast(new Message(Message.Mode.GROUP_CHAT, "SERVER", "ALL", msg.getSender() + " 접속", null));
                break;
            case DISCONNECT:
                broadcast(new Message(Message.Mode.GROUP_CHAT, "SERVER", "ALL", msg.getSender() + " 퇴장", null));
                break;
            case PRIVATE_CHAT:
                sendTo(msg.getReceiver(), msg);
                break;
            case GROUP_CHAT:
                broadcast(msg);
                break;
            case ORDER:
                sendTo("STAFF", msg); // 스태프에게 전달
                appendOrder(msg);     // 서버 로그에도 기록
                break;
            case EMOJI:
                sendTo(msg.getReceiver(), msg);
                break;
            case CHAT:
                broadcast(msg);
                break;
        }
    }

    public void sendTo(String pcNumber, Message msg) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                if (pcNumber.equals("ALL") || pcNumber.equals(c.getPcNumber())) {
                    c.sendMessage(msg);
                }
            }
        }
    }

    public void broadcast(Message msg) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                c.sendMessage(msg);
            }
        }
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
        appendLog("클라이언트 종료: " + handler.getPcNumber());
    }

    private void appendLogFiltered(Message msg) {
        boolean show;
        switch (msg.getMode()) {
            case CHAT: case PRIVATE_CHAT: case GROUP_CHAT: show = chkChat.isSelected(); break;
            case ORDER: show = chkOrder.isSelected(); break;
            case TIME_UPDATE: show = chkTime.isSelected(); break;
            case EMOJI: show = chkEmoji.isSelected(); break;
            default: show = true;
        }
        if (show) appendLog(msg.toString());
    }

    public void appendLog(String text) {
        SwingUtilities.invokeLater(() -> logArea.append(text + "\n"));
    }

    private void appendOrder(Message msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.getData() instanceof Order o) {
                orderArea.append(String.format("PC %s : %s\n", o.getPcNumber(), o.getMenuName()));
            } else {
                orderArea.append("주문 메시지: " + msg.getContent() + "\n");
            }
        });
    }

    // ✨ [핵심] ClientHandler 내부 클래스 (누락되었던 부분)
    class ClientHandler extends Thread {
        private Socket socket;
        private Server server;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String pcNumber = "UNKNOWN";

        public ClientHandler(Socket socket, Server server) {
            this.socket = socket;
            this.server = server;
        }

        public void setPcNumber(String pcNumber) {
            this.pcNumber = pcNumber;
        }

        public String getPcNumber() {
            return pcNumber;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    Message msg = (Message) in.readObject();
                    server.handleMessage(msg, this);
                }
            } catch (Exception e) {
                server.removeClient(this);
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        // ✨ [핵심] 동기화 처리된 전송 메서드 (메시지 씹힘 방지)
        public synchronized void sendMessage(Message msg) {
            try {
                out.writeObject(msg);
                out.flush();
                out.reset(); // 객체 캐시 초기화 (매우 중요)
            } catch (IOException e) {
                System.out.println("전송 실패 (" + pcNumber + "): " + e.getMessage());
            }
        }
    }
}

/*package server;

import common.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server extends JFrame {

    private ServerSocket serverSocket;
    private java.util.List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private Map<String, Integer> timeMap = Collections.synchronizedMap(new HashMap<>());

    private JTextArea logArea = new JTextArea();
    private JTextArea orderArea = new JTextArea();
    private JCheckBox chkChat = new JCheckBox("CHAT/PRIVATE/GROUP", true);
    private JCheckBox chkOrder = new JCheckBox("ORDER", true);
    private JCheckBox chkTime = new JCheckBox("TIME_UPDATE", true);
    private JCheckBox chkEmoji = new JCheckBox("EMOJI", true);

    private ScheduledExecutorService scheduler;

    public Server() {
        setTitle("PC방 서버 (직원용 중계 서버)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        buildUI();
        startTimeScheduler();
    }

    private void buildUI() {
        logArea.setEditable(false);
        orderArea.setEditable(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(chkChat);
        filterPanel.add(chkOrder);
        filterPanel.add(chkTime);
        filterPanel.add(chkEmoji);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(logArea),
                new JScrollPane(orderArea)
        );
        split.setDividerLocation(500);

        orderArea.setBorder(BorderFactory.createTitledBorder("주문 내역"));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(filterPanel, BorderLayout.NORTH);
        getContentPane().add(split, BorderLayout.CENTER);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            appendLog("서버 시작: port=" + port);

            Thread acceptThread = new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket socket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(socket, this);
                        clients.add(handler);
                        handler.start();
                    } catch (IOException e) {
                        appendLog("accept 에러: " + e.getMessage());
                    }
                }
            });
            acceptThread.setDaemon(true);
            acceptThread.start();

            setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버 시작 실패: " + e.getMessage());
        }
    }

    // 잔여시간 자동 감소 & TIME_UPDATE 브로드캐스트
    private void startTimeScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (timeMap) {
                for (Map.Entry<String, Integer> entry : timeMap.entrySet()) {
                    String pc = entry.getKey();
                    int t = entry.getValue();
                    if (t > 0) {
                        t -= 1;
                        entry.setValue(t);

                        TimeInfo info = new TimeInfo(t);
                        Message timeMsg = new Message(
                                Message.Mode.TIME_UPDATE,
                                "SERVER", pc,
                                "시간 업데이트", info
                        );
                        sendTo(pc, timeMsg);
                    }
                }
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    public void handleMessage(Message msg, ClientHandler sender) {
        // CONNECT일 때는 pcNumber 설정 및 기본 시간 부여
        if (msg.getMode() == Message.Mode.CONNECT) {
            String pc = msg.getSender();
            sender.setPcNumber(pc);
            synchronized (timeMap) {
                timeMap.putIfAbsent(pc, 120); // 기본 120분
            }
        }

        appendLogFiltered(msg);

        switch (msg.getMode()) {
            case CONNECT:
                broadcast(new Message(
                        Message.Mode.GROUP_CHAT,
                        "SERVER", "ALL",
                        msg.getSender() + " 접속",
                        null
                ));
                break;

            case DISCONNECT:
                broadcast(new Message(
                        Message.Mode.GROUP_CHAT,
                        "SERVER", "ALL",
                        msg.getSender() + " 퇴장",
                        null
                ));
                break;

            case PRIVATE_CHAT:
                sendTo(msg.getReceiver(), msg);
                break;

            case GROUP_CHAT:
                broadcast(msg);
                break;

            case ORDER:
                // 직원(=STAFF)에게만 전달
                sendTo("STAFF", msg);
                appendOrder(msg);
                break;

            case TIME_UPDATE:
                // 서버에서 직접 보내는 용도, 여기서는 클라이언트에서 보내지 않는다고 가정
                break;

            case EMOJI:
                // 이모티콘도 개인 채팅 형태로 처리
                sendTo(msg.getReceiver(), msg);
                break;

            case CHAT:
                // 필요하면 전체 채팅 등으로 사용
                broadcast(msg);
                break;
        }
    }

    public void sendTo(String pcNumber, Message msg) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                if (pcNumber.equals(c.getPcNumber())) {
                    c.send(msg);
                }
            }
        }
    }

    public void broadcast(Message msg) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                c.send(msg);
            }
        }
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
        appendLog("클라이언트 종료: " + handler.getPcNumber());
    }

    // 로그 출력 + 필터링
    private void appendLogFiltered(Message msg) {
        boolean show = false;
        switch (msg.getMode()) {
            case CHAT:
            case PRIVATE_CHAT:
            case GROUP_CHAT:
                show = chkChat.isSelected();
                break;
            case ORDER:
                show = chkOrder.isSelected();
                break;
            case TIME_UPDATE:
                show = chkTime.isSelected();
                break;
            case EMOJI:
                show = chkEmoji.isSelected();
                break;
            default:
                show = true;
        }
        if (show) {
            appendLog(msg.toString());
        }
    }

    public void appendLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text + "\n");
        });
    }

    private void appendOrder(Message msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.getData() instanceof Order o) {
                orderArea.append(
                        String.format("PC %s : %s\n", o.getPcNumber(), o.getMenuName())
                );
            } else {
                orderArea.append("주문 메시지: " + msg + "\n");
            }
        });
    }
}
*/