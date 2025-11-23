package client;

import common.*;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CustomerClient extends JFrame {

    private final ClientConnection conn = new ClientConnection();
    private String pcNumber;

    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JLabel timeLabel = new JLabel("잔여 시간: - 분");

    // ✅ 생성자: 여기서 UI만 만든다 (네가 말한 '생성자 없음' 부분 해결)
    public CustomerClient() {
        super("PC방 고객용 클라이언트");

        System.out.println(">>> CustomerClient 생성자 실행됨");
        buildUI();              // UI 세팅
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);       // 여기서 창을 띄운다
    }

    // ✅ 네트워크 연결 + 서버와 통신 시작
    public void start() {
        System.out.println(">>> CustomerClient.start() 진입");

        try {
            // 1) server.txt 읽기
            System.out.println("1. server.txt 읽는 중...");
            BufferedReader br = new BufferedReader(new FileReader("server.txt"));
            String ip = br.readLine();
            int port = Integer.parseInt(br.readLine());
            System.out.println("2. server.txt 읽기 완료: " + ip + ":" + port);

            // 2) 서버 연결
            conn.connect(ip, port);
            System.out.println("3. 서버 연결 성공");

            // 3) PC 번호 입력
            pcNumber = JOptionPane.showInputDialog(this, "PC 번호를 입력하세요", "PC01");
            if (pcNumber == null || pcNumber.isBlank()) {
                pcNumber = "UNKNOWN";
            }
            System.out.println("4. PC 번호 입력: " + pcNumber);

            // 상단 라벨 업데이트
            setTitle("PC방 고객용 클라이언트 - " + pcNumber);

            // 4) 서버에 CONNECT 메시지 전송
            Message connectMsg = new Message(
                    Message.Mode.CONNECT,
                    pcNumber,
                    "SERVER",
                    "connected",
                    null
            );
            conn.sendMessage(connectMsg);
            System.out.println("5. CONNECT 메시지 전송");

            // 5) 수신 스레드 시작
            conn.startReceiver(msg ->
                    SwingUtilities.invokeLater(() -> handleIncoming(msg))
            );
            System.out.println("6. 수신 스레드 시작");

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "server.txt 또는 서버 연결 오류: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "클라이언트 오류: " + e.getMessage());
        }
    }

    // ✅ UI 구성 (이전 코드 거의 그대로)
    private void buildUI() {
        setSize(500, 400);

        chatArea.setEditable(false);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("PC번호: "), BorderLayout.WEST);
        topPanel.add(timeLabel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton sendBtn = new JButton("보내기");
        JButton orderBtn = new JButton("주문");
        JButton emojiBtn = new JButton("이모티콘");
        buttonPanel.add(emojiBtn);
        buttonPanel.add(orderBtn);
        buttonPanel.add(sendBtn);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(chatArea), BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        // 리스너
        sendBtn.addActionListener(e -> sendChat());
        inputField.addActionListener(e -> sendChat());
        orderBtn.addActionListener(e -> sendOrder());
        emojiBtn.addActionListener(e -> sendEmoji());
    }

    private void sendChat() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        Message msg = new Message(
                Message.Mode.PRIVATE_CHAT,
                pcNumber,
                "STAFF",
                text,
                null
        );
        conn.sendMessage(msg);
        appendChat("나 ➜ STAFF: " + text);
        inputField.setText("");
    }

    private void sendOrder() {
        String[] menus = {"짜파게티", "김치볶음밥", "핫도그", "콜라", "사이다"};
        String menu = (String) JOptionPane.showInputDialog(
                this,
                "메뉴 선택",
                "주문하기",
                JOptionPane.PLAIN_MESSAGE,
                null,
                menus,
                menus[0]
        );
        if (menu == null) return;

        Order order = new Order(menu, pcNumber, System.currentTimeMillis());
        Message msg = new Message(
                Message.Mode.ORDER,
                pcNumber,
                "SERVER",
                "주문: " + menu,
                order
        );
        conn.sendMessage(msg);
        appendChat("[주문 보냄] " + menu);
    }

    private void sendEmoji() {
        String[] emojis = {":SMILE:", ":SAD:", ":LOL:", ":ANGRY:"};
        String selected = (String) JOptionPane.showInputDialog(
                this,
                "이모티콘 선택",
                "이모티콘",
                JOptionPane.PLAIN_MESSAGE,
                null,
                emojis,
                emojis[0]
        );
        if (selected == null) return;

        Message msg = new Message(
                Message.Mode.EMOJI,
                pcNumber,
                "STAFF",
                selected,
                null
        );
        conn.sendMessage(msg);
        appendChat("나 ➜ STAFF (이모티콘): " + selected);
    }

    private void handleIncoming(Message msg) {
        switch (msg.getMode()) {
            case PRIVATE_CHAT, GROUP_CHAT, CHAT -> {
                appendChat(msg.getSender() + " ➜ " + msg.getReceiver() + " : " + msg.getContent());
            }
            case ORDER -> {
                appendChat("[서버 응답] " + msg.getContent());
            }
            case TIME_UPDATE -> {
                if (msg.getData() instanceof TimeInfo info) {
                    timeLabel.setText("잔여 시간: " + info.getRemainingMinutes() + " 분");
                }
            }
            case EMOJI -> {
                appendChat(msg.getSender() + " ➜ " + msg.getReceiver() + " (이모티콘): " + msg.getContent());
            }
        }
    }

    private void appendChat(String line) {
        chatArea.append(line + "\n");
    }
}
