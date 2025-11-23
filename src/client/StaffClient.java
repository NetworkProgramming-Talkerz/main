package client;

import common.*;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;

public class StaffClient extends JFrame {

    private ClientConnection conn = new ClientConnection();
    private JTextArea chatArea = new JTextArea();
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

        // 서버에 STAFF로 CONNECT
        Message connectMsg = new Message(
                Message.Mode.CONNECT,
                "STAFF",
                "SERVER",
                "STAFF 접속",
                null
        );
        conn.sendMessage(connectMsg);

        conn.startReceiver(msg -> SwingUtilities.invokeLater(() -> handleIncoming(msg)));

        setVisible(true);
    }

    private void buildUI() {
        setTitle("PC방 직원용 클라이언트 (STAFF)");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatArea.setEditable(false);
        orderArea.setEditable(false);
        orderArea.setBorder(BorderFactory.createTitledBorder("주문 내역"));

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetPanel.add(new JLabel("대상 PC:"));
        targetPanel.add(targetPcField);
        topPanel.add(targetPanel, BorderLayout.WEST);

        JButton groupBtn = new JButton("전체 공지 보내기");
        topPanel.add(groupBtn, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);

        JButton sendBtn = new JButton("보내기");
        bottomPanel.add(sendBtn, BorderLayout.EAST);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(chatArea),
                new JScrollPane(orderArea)
        );
        split.setDividerLocation(420);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(split, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> sendPrivate());
        inputField.addActionListener(e -> sendPrivate());
        groupBtn.addActionListener(e -> sendGroup());
    }

    private void sendPrivate() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        String targetPc = targetPcField.getText().trim();
        if (targetPc.isEmpty()) targetPc = "ALL";

        Message msg = new Message(
                Message.Mode.PRIVATE_CHAT,
                "STAFF",
                targetPc,
                text,
                null
        );
        conn.sendMessage(msg);
        appendChat("STAFF ➜ " + targetPc + " : " + text);
        inputField.setText("");
    }

    private void sendGroup() {
        String text = JOptionPane.showInputDialog(this, "공지 내용 입력");
        if (text == null || text.isBlank()) return;

        Message msg = new Message(
                Message.Mode.GROUP_CHAT,
                "STAFF",
                "ALL",
                text,
                null
        );
        conn.sendMessage(msg);
        appendChat("[공지] " + text);
    }

    private void handleIncoming(Message msg) {
        switch (msg.getMode()) {
            case PRIVATE_CHAT, GROUP_CHAT, CHAT -> {
                appendChat(msg.getSender() + " ➜ " + msg.getReceiver() + " : " + msg.getContent());
            }
            case ORDER -> {
                if (msg.getData() instanceof Order o) {
                    orderArea.append(
                            String.format("PC %s : %s\n", o.getPcNumber(), o.getMenuName())
                    );
                } else {
                    orderArea.append("주문 메시지: " + msg + "\n");
                }
            }
            case TIME_UPDATE -> {
                // STAFF 쪽에서 전체 시간 모니터링하고 싶으면 여기서 핸들링 가능
            }
            case EMOJI -> {
                appendChat(msg.getSender() + " (이모티콘): " + msg.getContent());
            }
        }
    }

    private void appendChat(String line) {
        chatArea.append(line + "\n");
    }
}
