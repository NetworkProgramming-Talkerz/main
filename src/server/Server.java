package server;

import client.ui.*;      // ModernScrollBarUI 사용
import common.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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

public class
Server extends JFrame {

    private ServerSocket serverSocket;
    private List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    // PC별 잔여 시간
    private Map<String, Integer> timeMap = Collections.synchronizedMap(new HashMap<>());

    // PC 번호별 현재 접속 중인 클라이언트 (중복 접속 방지용)
    private final Map<String, ClientHandler> pcClientMap =
            Collections.synchronizedMap(new HashMap<>());

    private JTextArea logArea = new JTextArea();
    private JTextArea orderArea = new JTextArea();
    private JCheckBox chkChat = new JCheckBox("CHAT/PRIVATE/GROUP", true);
    private JCheckBox chkOrder = new JCheckBox("ORDER", true);
    private JCheckBox chkTime = new JCheckBox("TIME_UPDATE", true);
    private JCheckBox chkEmoji = new JCheckBox("EMOJI", true);

    private ScheduledExecutorService scheduler;

    public Server() {
        super("PC방 서버 (직원용 중계 서버)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        buildUI();
        startTimeScheduler();
    }

    /** 🎨 서버 화면을 고객 클라이언트처럼 다크 테마로 꾸미기 */
    private void buildUI() {
        Color bg = new Color(22, 22, 26);
        Color topBg = new Color(30, 30, 35);
        Color textMain = new Color(226, 232, 240);
        Color textSub = new Color(148, 163, 184);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(bg);

        // ─── 상단 바 (제목 + 필터 체크박스) ───
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(topBg);
        topBar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JLabel titleLabel = new JLabel("직원용 서버 모니터");
        titleLabel.setForeground(textMain);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        JLabel subLabel = new JLabel("클라이언트 접속 / 채팅 / 주문 / 시간 상태 모니터링");
        subLabel.setForeground(textSub);
        subLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(titleLabel);
        titleBox.add(subLabel);

        // 필터 체크박스 영역
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);

        JCheckBox[] filters = { chkChat, chkOrder, chkTime, chkEmoji };
        for (JCheckBox cb : filters) {
            cb.setOpaque(false);
            cb.setForeground(textSub);
            cb.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            filterPanel.add(cb);
        }

        topBar.add(titleBox, BorderLayout.WEST);
        topBar.add(filterPanel, BorderLayout.EAST);

        // ─── 로그 영역 ───
        logArea.setEditable(false);
        logArea.setBackground(bg);
        logArea.setForeground(textMain);
        logArea.setCaretColor(textMain);
        logArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(null);
        logScroll.getViewport().setBackground(bg);
        logScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        logScroll.getVerticalScrollBar().setUnitIncrement(16);

        // ─── 주문 영역 ───
        orderArea.setEditable(false);
        orderArea.setBackground(new Color(15, 23, 42));
        orderArea.setForeground(textMain);
        orderArea.setCaretColor(textMain);
        orderArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        orderArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane orderScroll = new JScrollPane(orderArea);
        orderScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81)),
                "주문 내역",
                TitledBorder.LEADING,
                TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 12),
                textSub
        ));
        orderScroll.getViewport().setBackground(new Color(15, 23, 42));
        orderScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        orderScroll.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                logScroll,
                orderScroll
        );
        split.setDividerLocation(580);
        split.setDividerSize(2);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setBackground(bg);

        getContentPane().add(topBar, BorderLayout.NORTH);
        getContentPane().add(split, BorderLayout.CENTER);
    }

    /** 특정 PC의 현재 시간을 PC + STAFF 둘 다에게 보내기 */
    private void sendTimeUpdate(String pc, int minutes) {
        // 1) 해당 PC
        Message timeMsg = new Message(
                Message.Mode.TIME_UPDATE,
                "SERVER",
                pc,
                "시간",
                new TimeInfo(minutes)
        );
        sendTo(pc, timeMsg);

        // 2) STAFF (sender = PC번호)
        Message staffMsg = new Message(
                Message.Mode.TIME_UPDATE,
                pc,
                "STAFF",
                "시간",
                new TimeInfo(minutes)
        );
        sendTo("STAFF", staffMsg);
    }

    /** ⏱ 잔여시간 자동 감소 & TIME_UPDATE 전송 */
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
                        // 변경된 시간 전파
                        sendTimeUpdate(pc, t);
                    }
                }
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    /** 서버 시작 */
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

    /** 클라이언트에서 온 메시지 처리 */
    public void handleMessage(Message msg, ClientHandler sender) {

        // ✨ CONNECT 시 PC 번호와 초기 잔여 시간 설정 + 중복 PC 체크
        if (msg.getMode() == Message.Mode.CONNECT) {
            String pc = msg.getSender();   // "PC01" / "PC02" / "STAFF"

            // STAFF가 아닌 경우에만 중복 PC 체크
            if (!"STAFF".equals(pc)) {
                synchronized (pcClientMap) {
                    if (pcClientMap.containsKey(pc)) {
                        Message deny = new Message(
                                Message.Mode.DUPLICATE_PC,
                                "SERVER",
                                pc,
                                "이미 " + pc + " 이(가) 접속해 있습니다!",
                                null
                        );
                        sender.sendMessage(deny);
                        appendLog("중복 접속 시도: " + pc);
                        return;
                    }
                    pcClientMap.put(pc, sender);
                }
            }

            sender.setPcNumber(pc);

            // STAFF는 시간 관리 X
            if (!"STAFF".equals(pc)) {
                int minutes = 120;
                Object data = msg.getData();
                if (data instanceof TimeInfo ti) {
                    minutes = ti.getRemainingMinutes();
                }

                synchronized (timeMap) {
                    timeMap.put(pc, minutes);
                }

                // 접속 직후 현재 잔여 시간을 PC + STAFF에게 전송
                sendTimeUpdate(pc, minutes);
            }
        }

        // 로그는 기존 필터대로
        appendLogFiltered(msg);

        switch (msg.getMode()) {
            case CONNECT:
                // 접속 공지는 STAFF 에게만
                sendTo("STAFF", new Message(
                        Message.Mode.CONNECT_NOTICE,
                        "SERVER",
                        "STAFF",
                        msg.getSender() + " 접속",
                        null
                ));
                break;

            case DISCONNECT: {
                String pc = msg.getSender();

                synchronized (pcClientMap) {
                    pcClientMap.remove(pc);
                }
                synchronized (timeMap) {
                    timeMap.remove(pc);
                }

                sendTo("STAFF", new Message(
                        Message.Mode.CONNECT_NOTICE,
                        "SERVER",
                        "STAFF",
                        pc + " 퇴장",
                        null
                ));
                break;
            }

            case PRIVATE_CHAT:
                sendTo(msg.getReceiver(), msg);
                break;

            case GROUP_CHAT:
                broadcast(msg);
                break;

            case ORDER:
                sendTo("STAFF", msg);
                appendOrder(msg);
                break;

            case EMOJI:
                sendTo(msg.getReceiver(), msg);
                break;

            // 🔹 직원이 시간 추가
            case ADD_TIME: {
                String targetPc = msg.getReceiver();
                int addMinutes = 0;
                if (msg.getData() instanceof TimeInfo ti) {
                    addMinutes = ti.getRemainingMinutes();   // 추가할 분
                }

                int newVal;
                synchronized (timeMap) {
                    int cur = timeMap.getOrDefault(targetPc, 0);
                    newVal = cur + addMinutes;
                    if (newVal < 0) newVal = 0;
                    timeMap.put(targetPc, newVal);
                }
                appendLog("시간 변경: " + targetPc + " -> " + newVal + "분 (+" + addMinutes + ")");
                sendTimeUpdate(targetPc, newVal);
                break;
            }

            // 🔹 직원이 강제 종료
            case FORCE_LOGOUT: {
                String targetPc = msg.getReceiver();
                ClientHandler target;
                synchronized (pcClientMap) {
                    target = pcClientMap.get(targetPc);
                }
                if (target != null) {
                    target.sendMessage(new Message(
                            Message.Mode.FORCE_LOGOUT,
                            "SERVER",
                            targetPc,
                            "관리자에 의해 이용이 종료되었습니다.",
                            null
                    ));
                    appendLog("강제 종료 명령 전송: " + targetPc);
                } else {
                    appendLog("강제 종료 실패: 접속 중이 아닌 PC " + targetPc);
                }
                break;
            }

            case CHAT:
                broadcast(msg);
                break;

            default:
                // TIME_UPDATE 등은 위에서 이미 처리함
                break;
        }
    }

    /** 특정 PC 또는 ALL 에게 전송 */
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

        String pc = handler.getPcNumber();
        if (pc != null) {
            synchronized (pcClientMap) {
                pcClientMap.remove(pc);
            }
            synchronized (timeMap) {
                timeMap.remove(pc);
            }
            appendLog("클라이언트 종료: " + pc);
        } else {
            appendLog("클라이언트 종료: UNKNOWN");
        }
    }

    private void appendLogFiltered(Message msg) {
        boolean show;
        switch (msg.getMode()) {
            case CHAT:
            case PRIVATE_CHAT:
            case GROUP_CHAT:
            case CONNECT_NOTICE:
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
        if (show) appendLog(msg.toString());
    }

    public void appendLog(String text) {
        SwingUtilities.invokeLater(() -> logArea.append(text + "\n"));
    }

    private void appendOrder(Message msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.getData() instanceof Order o) {
                orderArea.append(
                        String.format("PC %s : %s\n", o.getPcNumber(), o.getMenuName())
                );
            } else {
                orderArea.append("주문 메시지: " + msg.getContent() + "\n");
            }
        });
    }

    // ───────────────── ClientHandler ─────────────────
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

        // 동기화된 전송 메서드
        public synchronized void sendMessage(Message msg) {
            try {
                out.writeObject(msg);
                out.flush();
                out.reset(); // 객체 캐시 초기화
            } catch (IOException e) {
                System.out.println("전송 실패 (" + pcNumber + "): " + e.getMessage());
            }
        }
    }
}
