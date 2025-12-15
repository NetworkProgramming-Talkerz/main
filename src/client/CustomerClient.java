package client;

import client.ui.*;
import common.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CustomerClient extends JFrame {

    private final ClientConnection conn = new ClientConnection();
    private String pcNumber;

    private final BubbleChatPanel chatPanel = new BubbleChatPanel();
    private final JTextField inputField = new JTextField();
    private final JLabel timeLabel = new JLabel("잔여 시간: - 분");

    private int orderCount = 0;

    // 정산용
    private int usageHour = 0;       // 이용 시간 (시간 단위)
    private int currentFoodCost = 0; // 음식 주문 총액
    private static final int HOURLY_RATE = 1000;
    private List<String> orderHistory = new ArrayList<>();

    // 전광판 배너
    private JPanel marqueePanel;
    private JLabel marqueeLabel;
    private javax.swing.Timer marqueeTimer;
    private boolean notice5Shown = false;
    private boolean notice1Shown = false;

    public CustomerClient() {
        super("PC방 고객용 클라이언트");
        buildUI();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

    }

    public void start() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("server.txt"));
            String ip = br.readLine();
            int port = Integer.parseInt(br.readLine());
            conn.connect(ip, port);

            // 1. PC 번호 입력
            pcNumber = JOptionPane.showInputDialog(this, "PC 번호 입력", "PC01");
            if (pcNumber == null || pcNumber.isBlank()) pcNumber = "UNKNOWN";

            // 2. 이용 시간 입력
            String hourInput = JOptionPane.showInputDialog(
                    this,
                    "이용하실 시간을 입력하세요 (시간 단위)",
                    "1"
            );
            try {
                usageHour = Integer.parseInt(hourInput.trim());
            } catch (Exception e) {
                usageHour = 1;
            }

            setTitle("PC방 고객용 (" + pcNumber + ") - " + usageHour + "시간 이용중");

            int initialMinutes = usageHour * 60;

            // 3. CONNECT 전송 (초기 잔여시간 포함)
            conn.sendMessage(new Message(
                    Message.Mode.CONNECT,
                    pcNumber,
                    "SERVER",
                    "connected",
                    new TimeInfo(initialMinutes)
            ));

            // 4. 수신 쓰레드 시작
            conn.startReceiver(msg ->
                    SwingUtilities.invokeLater(() -> handleIncoming(msg))
            );

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "오류: " + e.getMessage());
        }
    }

    private void buildUI() {
        setSize(520, 480);
        getContentPane().setBackground(new Color(22, 22, 26));
        chatPanel.setBackground(new Color(22, 22, 26));

        JScrollPane scroll = new JScrollPane(chatPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getViewport().setBackground(new Color(22, 22, 26));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // 상단 (영수증 + 잔여시간)
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(30, 30, 35));

        ModernButton receiptBtn = new ModernButton("🧾 영수증 조회", new Color(40, 40, 50));
        receiptBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        receiptBtn.setPreferredSize(new Dimension(120, 30));
        receiptBtn.addActionListener(e -> showReceipt());

        top.add(receiptBtn, BorderLayout.WEST);

        timeLabel.setForeground(Color.white);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        top.add(timeLabel, BorderLayout.EAST);

        // 전광판
        marqueePanel = new JPanel(null);
        marqueePanel.setPreferredSize(new Dimension(10, 30));
        marqueePanel.setBackground(new Color(22, 22, 26));
        marqueePanel.setVisible(false);

        marqueeLabel = new JLabel();
        marqueeLabel.setForeground(Color.YELLOW);
        marqueeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        marqueePanel.add(marqueeLabel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(22, 22, 26));
        centerPanel.add(marqueePanel, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(30, 30, 35));

        inputField.setBackground(new Color(45, 45, 55));
        inputField.setForeground(Color.white);
        inputField.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        ModernButton sendBtn  = new ModernButton("보내기");
        ModernButton emojiBtn = new ModernButton("😊", new Color(60, 60, 70));
        emojiBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        ModernButton orderBtn = new ModernButton("🍜", new Color(60, 60, 70));
        orderBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        bottom.add(inputField, BorderLayout.CENTER);

        JPanel rightBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBtn.setOpaque(false);
        rightBtn.add(sendBtn);
        rightBtn.add(emojiBtn);
        rightBtn.add(orderBtn);


        bottom.add(rightBtn, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> sendChat());
        inputField.addActionListener(e -> sendChat());

        //emojiBtn.addActionListener(e -> emojiPicker.show(emojiBtn));
        emojiBtn.addActionListener(e -> selectAndSendImage());

        orderBtn.addActionListener(e -> sendOrder());

        inputField.setBackground(new Color(45, 45, 55));
        inputField.setForeground(Color.white);

// 👇 [추가] 배경이 어두우니 커서를 '흰색'으로 변경
        inputField.setCaretColor(Color.WHITE);
    }

    // [수정] 이모티콘 버튼 클릭 시 실행되는 메서드
    private void selectAndSendImage() {
        // 이모티콘 선택창 띄우기 (람다식으로 선택 후 동작 정의)
        new EmoticonPicker(this, (imgData) -> {
            try {
                // 1. 서버로 전송
                conn.sendMessage(new Message(
                        Message.Mode.EMOJI,
                        pcNumber,
                        "STAFF",
                        "[이모티콘]", // 텍스트 로그용
                        imgData      // 바이트 데이터 (리소스에서 읽은 것)
                ));

                // 2. 내 채팅창에도 표시
                chatPanel.addImageBubble(imgData, true);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "이모티콘 전송 실패: " + ex.getMessage());
            }
        }).setVisible(true);
    }

    private void sendChat() {
        String t = inputField.getText().trim();
        if (t.isEmpty()) return;

        conn.sendMessage(new Message(
                Message.Mode.PRIVATE_CHAT, pcNumber, "STAFF", t, null
        ));

        chatPanel.addBubble( t, true);
        inputField.setText("");
    }


    private void sendOrder() {
        new OrderDialog(this, (orderText, totalPrice) -> {

            currentFoodCost += totalPrice;
            orderHistory.add(String.format("- %s (%,d원)", orderText, totalPrice));
            orderCount++;

            Order o = new Order(orderText, pcNumber, totalPrice, System.currentTimeMillis());
            String summary = String.format("[주문] %02d번 주문 (총 %,d원)", orderCount, totalPrice);

            conn.sendMessage(new Message(
                    Message.Mode.ORDER, pcNumber, "SERVER", summary, o
            ));

            chatPanel.addBubble(summary, true);

        }).setVisible(true);
    }

    // 영수증 조회
    private void showReceipt() {
        int timeFee = usageHour * HOURLY_RATE;
        int totalFee = timeFee + currentFoodCost;

        StringBuilder sb = new StringBuilder();
        sb.append("========== [상세 영수증] ==========\n\n");
        sb.append("[이용 내역]\n");
        sb.append(String.format("💻 이용 시간 (%d시간) : %,d원\n", usageHour, timeFee));
        sb.append("----------------------------------\n");
        sb.append("[주문 내역]\n");
        if (orderHistory.isEmpty()) {
            sb.append("(주문 내역 없음)\n");
        } else {
            for (String history : orderHistory) {
                sb.append(history).append("\n");
            }
        }
        sb.append("----------------------------------\n");
        sb.append(String.format("🍜 주문 총액 : %,d원\n", currentFoodCost));
        sb.append("==================================\n");
        sb.append(String.format("💰 총 결제금액 : %,d원\n", totalFee));

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(ta);
        scroll.setPreferredSize(new Dimension(350, 400));

        JOptionPane.showMessageDialog(this, scroll, "영수증 조회", JOptionPane.PLAIN_MESSAGE);
    }


    // 🔹 서버에서 온 메시지 처리
    private void handleIncoming(Message msg) {
        switch (msg.getMode()) {
            case PRIVATE_CHAT, CHAT ->
                    chatPanel.addBubble(msg.getSender() + ": " + msg.getContent(), false);

            case GROUP_CHAT ->
                    showMarquee("📢 [공지] " + msg.getContent());
                    //chatPanel.addBubble("[공지] " + msg.getContent(), false);

            case ORDER ->
                    chatPanel.addBubble("[서버] " + msg.getContent(), false);

            case TIME_UPDATE -> {
                if (msg.getData() instanceof TimeInfo i) {
                    int remain = i.getRemainingMinutes();
                    timeLabel.setText("잔여 시간: " + remain + "분");

                    if (remain == 5 && !notice5Shown) {
                        showMarquee("이용시간이 5분 남았습니다.");
                        notice5Shown = true;
                    }
                    if (remain == 1 && !notice1Shown) {
                        showMarquee("이용시간이 1분 남았습니다.");
                        notice1Shown = true;
                    }
                }
            }

            case EMOJI -> {
                if (msg.getData() instanceof byte[] imgData) {
                    chatPanel.addImageBubble(imgData, false);
                }
            }


            // 🔻 중복 PC 접속 거절
            case DUPLICATE_PC -> {
                JOptionPane.showMessageDialog(
                        this,
                        msg.getContent(),          // "이미 PC01 이(가) 접속해 있습니다!"
                        "중복 접속",
                        JOptionPane.WARNING_MESSAGE
                );

                dispose();
                System.exit(0);
            }

            // 🔻 STAFF가 강제 종료 시킨 경우
            case FORCE_LOGOUT -> {
                JOptionPane.showMessageDialog(
                        this,
                        msg.getContent() == null
                                ? "관리자에 의해 이용이 종료되었습니다."
                                : msg.getContent(),
                        "강제 종료",
                        JOptionPane.WARNING_MESSAGE
                );

                // 필요하면 여기서 conn.close() 같은 정리 로직도 추가
                dispose();
                System.exit(0);
            }
        }
    }


    // 전광판 배너 (좌 -> 우로 왔다갔다 / 20초 제한)
    private void showMarquee(String text) {
        if (marqueeTimer != null && marqueeTimer.isRunning()) {
            marqueeTimer.stop();
        }

        marqueeLabel.setText(text);

        int panelWidth = marqueePanel.getWidth();
        if (panelWidth <= 0) {
            panelWidth = getWidth();
        }
        int panelHeight = marqueePanel.getPreferredSize().height;
        int labelWidth = marqueeLabel.getPreferredSize().width;

        final int startX = -labelWidth;
        marqueeLabel.setBounds(startX, 0, labelWidth, panelHeight);

        marqueePanel.setVisible(true);

        final int speed = 4;
        final long endTime = System.currentTimeMillis() + 20_000L;
        final int panelWidthFinal = panelWidth;

        marqueeTimer = new javax.swing.Timer(40, e -> {
            int x = marqueeLabel.getX() + speed;
            if (x > panelWidthFinal) {
                x = startX;
            }
            marqueeLabel.setLocation(x, marqueeLabel.getY());

            long now = System.currentTimeMillis();
            if (now >= endTime) {
                ((javax.swing.Timer) e.getSource()).stop();
                marqueePanel.setVisible(false);
            }
        });

        marqueeTimer.start();
    }
}
