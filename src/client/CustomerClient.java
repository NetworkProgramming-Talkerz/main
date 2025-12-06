
package client;

import client.ui.*;
import common.*;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CustomerClient extends JFrame {

    private final ClientConnection conn = new ClientConnection();
    private String pcNumber;

    private final BubbleChatPanel chatPanel = new BubbleChatPanel();
    private final JTextField inputField = new JTextField();
    private final JLabel timeLabel = new JLabel("잔여 시간: - 분");

    private int orderCount = 0;

    // ✨ [추가] 정산용 변수들
    private int usageHour = 0;       // 이용 시간 (단위: 시간)
    private int currentFoodCost = 0; // 현재까지 음식 주문 총액
    private static final int HOURLY_RATE = 1000; // 시간당 요금 (예: 1000원)
    private List<String> orderHistory = new ArrayList<>(); // 📝 먹은 메뉴들 기록장

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

            // ✨ 2. [추가] 이용 시간 입력 (정수값)
            String hourInput = JOptionPane.showInputDialog(this, "이용하실 시간을 입력하세요 (시간 단위)", "2");
            try {
                usageHour = Integer.parseInt(hourInput);
            } catch (NumberFormatException e) {
                usageHour = 1; // 잘못 입력하면 기본 1시간
            }

            setTitle("PC방 고객용 (" + pcNumber + ") - " + usageHour + "시간 이용중");

            // 서버 연결 메시지 전송
            conn.sendMessage(new Message(
                    Message.Mode.CONNECT, pcNumber, "SERVER", "connected", null
            ));

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
        // ✨ [추가] 커스텀 스크롤바 UI 적용 (아래 클래스 필요)
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getViewport().setBackground(new Color(22, 22, 26));

        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(30, 30, 35));

        // ✨ [추가] 영수증 조회 버튼 (왼쪽에 배치)
        ModernButton receiptBtn = new ModernButton("🧾 영수증 조회", new Color(40, 40, 50));
        receiptBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        receiptBtn.setPreferredSize(new Dimension(120, 30));
        receiptBtn.addActionListener(e -> showReceipt()); // 버튼 누르면 영수증 보여주기

        top.add(receiptBtn, BorderLayout.WEST); // 라벨 자리에 버튼 쏙 넣기

        timeLabel.setForeground(Color.white);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // 오른쪽 여백 살짝
        top.add(timeLabel, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(30, 30, 35));

        inputField.setBackground(new Color(45, 45, 55));
        inputField.setForeground(Color.white);
        inputField.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));

        // 👇 [수정 코드] ModernButton으로 교체
        ModernButton sendBtn = new ModernButton("보내기");

        // 이모티콘과 주문 버튼은 회색으로 만들면 더 예쁩니다 (구분감)
        ModernButton emojiBtn = new ModernButton("😊", new Color(60, 60, 70));
        emojiBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        ModernButton orderBtn = new ModernButton("🍜", new Color(60, 60, 70));
        orderBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        bottom.add(inputField, BorderLayout.CENTER);

        JPanel rightBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBtn.setOpaque(false);
        rightBtn.add(emojiBtn);
        rightBtn.add(orderBtn);
        rightBtn.add(sendBtn);

        bottom.add(rightBtn, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> sendChat());
        inputField.addActionListener(e -> sendChat());
        emojiBtn.addActionListener(e -> sendEmoji());
        orderBtn.addActionListener(e -> sendOrder());
    }


    private void sendChat() {
        String t = inputField.getText().trim();
        if (t.isEmpty()) return;

        conn.sendMessage(new Message(
                Message.Mode.PRIVATE_CHAT, pcNumber, "STAFF", t, null
        ));

        chatPanel.addBubble("나: " + t, true);
        inputField.setText("");
    }

    private void sendOrder() {
        // totalPrice는 '이번 주문'에 대한 가격입니다.
        new OrderDialog(this, (orderText, totalPrice) -> {

            // 1. 누적 금액 업데이트 (영수증용)
            currentFoodCost += totalPrice;

            // 2. 주문 내역 기록 (영수증용)
            orderHistory.add(String.format("- %s (%,d원)", orderText, totalPrice));

            // 3. 주문 번호 증가
            orderCount++;

            // 4. Order 객체 생성 (여기에 '상세 내역(orderText)'을 담습니다!)
            // 생성자 순서: 메뉴내역, PC번호, 가격, 시간
            Order o = new Order(orderText, pcNumber, totalPrice, System.currentTimeMillis());

            // 5. 메시지 전송 (내용은 '요약'만 보냄)
            // 예: "[주문] 01번 주문 (총 4,500원)"
            String summary = String.format("[주문] %02d번 주문 (총 %,d원)", orderCount, totalPrice);

            conn.sendMessage(new Message(
                    Message.Mode.ORDER, pcNumber, "SERVER", summary, o
            ));

            // 6. 내 채팅창에도 '요약'만 표시
            chatPanel.addBubble(summary, true);

        }).setVisible(true);
    }
    // ✨ 2. [신규] 영수증 조회 메서드
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
            // ✨ 기록된 메뉴들을 하나씩 꺼내서 찍어주기
            for (String history : orderHistory) {
                sb.append(history).append("\n");
            }
        }

        sb.append("----------------------------------\n");
        sb.append(String.format("🍜 주문 총액 : %,d원\n", currentFoodCost));
        sb.append("==================================\n");
        sb.append(String.format("💰 총 결제금액 : %,d원\n", totalFee));

        // JTextArea를 사용해서 스크롤이 가능한 팝업으로 보여줌 (내역이 길어질 수 있으니까)
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(ta);
        scroll.setPreferredSize(new Dimension(350, 400)); // 팝업창 크기 지정

        JOptionPane.showMessageDialog(this, scroll, "영수증 조회", JOptionPane.PLAIN_MESSAGE);
    }

    private void sendEmoji() {
        String[] e = {":)", ":(", ":D", ":O"};
        String sel = (String) JOptionPane.showInputDialog(
                this, "이모티콘", "선택", JOptionPane.PLAIN_MESSAGE, null, e, e[0]
        );
        if (sel == null) return;

        conn.sendMessage(new Message(
                Message.Mode.EMOJI, pcNumber, "STAFF", sel, null
        ));
        chatPanel.addBubble("나: " + sel, true);
    }

    private void handleIncoming(Message msg) {
        switch (msg.getMode()) {
            case PRIVATE_CHAT, CHAT ->
                    chatPanel.addBubble(msg.getSender() + ": " + msg.getContent(), false);

            case GROUP_CHAT ->
                    chatPanel.addBubble("[공지] " + msg.getContent(), false);

            case ORDER ->
                    chatPanel.addBubble("[서버] " + msg.getContent(), false);

            case TIME_UPDATE -> {
                if (msg.getData() instanceof TimeInfo i)
                    timeLabel.setText("잔여 시간: " + i.getRemainingMinutes() + "분");
            }

            case EMOJI ->
                    chatPanel.addBubble(msg.getSender() + ": " + msg.getContent(), false);
        }
    }
}
