package client;

import client.ui.*;
import common.*;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;

public class StaffClient extends JFrame {

    private ClientConnection conn = new ClientConnection();
    private BubbleChatPanel chatPanel = new BubbleChatPanel();
    private JTextField inputField = new JTextField();
    private JTextField targetPcField = new JTextField("PC01");
    private JTextArea orderArea = new JTextArea();

    public void start() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader("server.txt"))) {
            String ip = br.readLine();
            int port = Integer.parseInt(br.readLine());
            conn.connect(ip, port);
        }

        buildUI();

        conn.sendMessage(new Message(
                Message.Mode.CONNECT, "STAFF", "SERVER", "STAFF 접속", null
        ));

        conn.startReceiver(msg ->
                SwingUtilities.invokeLater(() -> handleIncoming(msg))
        );

        setVisible(true);
    }

    private void buildUI() {
        setTitle("PC방 직원용 클라이언트");
        setSize(800, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(new Color(22, 22, 26));
        orderArea.setEditable(false);
        orderArea.setBackground(new Color(30, 30, 35));
        orderArea.setForeground(Color.white);
        orderArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14)); // 폰트 설정
        orderArea.setBorder(BorderFactory.createTitledBorder("주문 내역"));

        JScrollPane chatScroll = new JScrollPane(chatPanel);
        chatScroll.setBorder(null);
        chatScroll.getVerticalScrollBar().setUI(new client.ui.ModernScrollBarUI());
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        chatScroll.getViewport().setBackground(new Color(22, 22, 26));

        JScrollPane orderScroll = new JScrollPane(orderArea);
        orderScroll.setBorder(null);
        orderScroll.getVerticalScrollBar().setUI(new client.ui.ModernScrollBarUI());
        orderScroll.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                chatScroll,
                orderScroll
        );
        split.setDividerLocation(480);
        split.setBorder(null);
        split.setDividerSize(1);
        split.setBackground(new Color(22, 22, 26));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(30, 30, 35));
        JLabel lbl = new JLabel("대상 PC:");
        lbl.setForeground(Color.white);

        targetPcField.setBackground(new Color(40, 40, 50));
        targetPcField.setForeground(Color.white);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.setOpaque(false);
        left.add(lbl);
        left.add(targetPcField);

        ModernButton noticeBtn = new ModernButton("전체 공지", new Color(220, 50, 50));
        top.add(left, BorderLayout.WEST);
        top.add(noticeBtn, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(30, 30, 35));

        inputField.setBackground(new Color(45, 45, 55));
        inputField.setForeground(Color.white);

        ModernButton sendBtn = new ModernButton("보내기");
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendBtn, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> sendPrivate());
        inputField.addActionListener(e -> sendPrivate());
        noticeBtn.addActionListener(e -> sendGroup());
    }

    private void sendPrivate() {
        String t = inputField.getText().trim();
        if (t.isEmpty()) return;
        String pc = targetPcField.getText().trim();
        if (pc.isBlank()) pc = "ALL";
        conn.sendMessage(new Message(Message.Mode.PRIVATE_CHAT, "STAFF", pc, t, null));
        chatPanel.addBubble("STAFF: " + t, true);
        inputField.setText("");
    }

    private void sendGroup() {
        String text = JOptionPane.showInputDialog(this, "공지 내용");
        if (text == null || text.isBlank()) return;
        conn.sendMessage(new Message(Message.Mode.GROUP_CHAT, "STAFF", "ALL", text, null));
        chatPanel.addBubble("[공지] " + text, true);
    }

    private void handleIncoming(Message msg) {
        switch (msg.getMode()) {
            case PRIVATE_CHAT, GROUP_CHAT, CHAT ->
                    chatPanel.addBubble(msg.getSender() + ": " + msg.getContent(), false);

            case ORDER -> {
                String pcNum = msg.getSender();
                String summary = msg.getContent(); // "[주문] 01번 주문 (총 13,500원)"

                // 1. 왼쪽 채팅창 (변경 없음)
                chatPanel.addBubble(pcNum + ": " + summary, false);

                // 2. 오른쪽 주문 패널 (디자인 변경)
                if (msg.getData() instanceof Order o) {
                    String time = new java.text.SimpleDateFormat("HH:mm:ss")
                            .format(new java.util.Date(o.getTimestamp()));

                    String[] menuList = o.getMenuName().split(", ");

                    // 1️⃣ 헤더 출력 (시간 제거함)
                    // 기존: "█ PC01 [주문]... _17:48:08"
                    // 변경: "█ PC01 [주문]..."
                    orderArea.append(String.format("█ %s %s\n", pcNum, summary));

                    // 2️⃣ 상세 메뉴 출력
                    for (String menu : menuList) {
                        orderArea.append("   - " + menu + "\n");
                    }

                    // 3️⃣ 푸터 출력 (시간을 오른쪽 구석으로 밀어버림)
                    // 공백을 넉넉히 넣어서 오른쪽 정렬 느낌을 냅니다.
                    orderArea.append("                                            " + time + "\n\n");

                } else {
                    orderArea.append("🚨 주문 오류: 데이터 없음\n");
                }

                // 스크롤 맨 아래로
                orderArea.setCaretPosition(orderArea.getDocument().getLength());
            }

            case EMOJI -> chatPanel.addBubble(msg.getSender() + ": " + msg.getContent(), false);
        }
    }
}