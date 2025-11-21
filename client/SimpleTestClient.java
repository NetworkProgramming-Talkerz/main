package client;

import common.ChatMsg;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class SimpleTestClient extends JFrame {
    private JTextField txtIP, txtPort, txtID, txtMessage, txtReceiver;
    private JCheckBox chkAdmin; // 관리자 여부 체크
    private JTextArea txtLog;
    private JComboBox<String> comboMode; // 보낼 메시지 종류 선택

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public SimpleTestClient() {
        super("서버 기능 테스트용 클라이언트");
        buildGUI();
        setSize(600, 500);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void buildGUI() {
        setLayout(new BorderLayout());

        // [상단] 접속 설정
        JPanel pnlTop = new JPanel(new GridLayout(2, 1));
        JPanel pnlConn = new JPanel(new FlowLayout(FlowLayout.LEFT));

        txtIP = new JTextField("127.0.0.1", 8);
        txtPort = new JTextField("54321", 4);
        txtID = new JTextField("PC01", 6);
        chkAdmin = new JCheckBox("관리자 모드");
        JButton btnConnect = new JButton("접속");

        pnlConn.add(new JLabel("IP:")); pnlConn.add(txtIP);
        pnlConn.add(new JLabel("Port:")); pnlConn.add(txtPort);
        pnlConn.add(new JLabel("ID:")); pnlConn.add(txtID);
        pnlConn.add(chkAdmin);
        pnlConn.add(btnConnect);

        // [상단] 메시지 전송 설정
        JPanel pnlMsgSet = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] modes = {"전체채팅", "1:1귓속말", "상품주문"};
        comboMode = new JComboBox<>(modes);
        txtReceiver = new JTextField(6);
        txtReceiver.setBorder(BorderFactory.createTitledBorder("받는사람ID"));

        pnlMsgSet.add(new JLabel("모드:"));
        pnlMsgSet.add(comboMode);
        pnlMsgSet.add(txtReceiver);

        pnlTop.add(pnlConn);
        pnlTop.add(pnlMsgSet);
        add(pnlTop, BorderLayout.NORTH);

        // [중앙] 로그
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        add(new JScrollPane(txtLog), BorderLayout.CENTER);

        // [하단] 입력창
        JPanel pnlBot = new JPanel(new BorderLayout());
        txtMessage = new JTextField();
        JButton btnSend = new JButton("전송");
        pnlBot.add(txtMessage, BorderLayout.CENTER);
        pnlBot.add(btnSend, BorderLayout.EAST);
        add(pnlBot, BorderLayout.SOUTH);

        // --- 이벤트 리스너 ---

        // 1. 접속 버튼
        btnConnect.addActionListener(e -> connectToServer());

        // 2. 전송 버튼
        btnSend.addActionListener(e -> sendMessage());
        txtMessage.addActionListener(e -> sendMessage()); // 엔터키 처리
    }

    // 서버 접속 로직
    private void connectToServer() {
        try {
            socket = new Socket(txtIP.getText(), Integer.parseInt(txtPort.getText()));
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // 접속하자마자 로그인 메시지 전송
            int loginMode = chkAdmin.isSelected() ? ChatMsg.MODE_ADMIN_LOGIN : ChatMsg.MODE_LOGIN;
            String myID = txtID.getText();

            send(new ChatMsg(myID, loginMode)); // 로그인 패킷 전송

            // 수신 스레드 시작
            new Thread(this::receiveMessage).start();

            txtLog.append("✅ 서버에 접속했습니다.\n");

        } catch (Exception e) {
            txtLog.append("❌ 접속 실패: " + e.getMessage() + "\n");
        }
    }

    // 메시지 전송 로직
    private void sendMessage() {
        if (out == null) return;

        String msgText = txtMessage.getText();
        if(msgText.isEmpty()) return;

        String myID = txtID.getText();
        String receiver = txtReceiver.getText();
        int selectedIdx = comboMode.getSelectedIndex();
        ChatMsg msg = null;

        // 모드에 따라 메시지 생성
        switch (selectedIdx) {
            case 0: // 전체 채팅
                msg = new ChatMsg(myID, ChatMsg.MODE_TX_STRING, msgText);
                break;
            case 1: // 1:1 귓속말
                if(receiver.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "받는 사람 ID를 입력하세요.");
                    return;
                }
                msg = new ChatMsg(myID, receiver, ChatMsg.MODE_PRIVATE_CHAT, msgText);
                break;
            case 2: // 상품 주문
                msg = new ChatMsg(myID, null, ChatMsg.MODE_TX_ORDER, msgText); // 주문은 받는사람 필요없음(자동 관리자)
                break;
        }

        send(msg);
        txtMessage.setText(""); // 입력창 비우기
    }

    // 실제 패킷 전송
    private void send(ChatMsg msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {
            txtLog.append("❌ 전송 오류\n");
        }
    }

    // 서버로부터 메시지 수신 (Thread)
    private void receiveMessage() {
        try {
            while (true) {
                ChatMsg msg = (ChatMsg) in.readObject();

                String sender = msg.userID;
                String txt = msg.message;

                // 받은 메시지 화면에 출력
                if (msg.mode == ChatMsg.MODE_TX_STRING) {
                    txtLog.append("[전체] " + sender + ": " + txt + "\n");
                } else if (msg.mode == ChatMsg.MODE_PRIVATE_CHAT) {
                    txtLog.append("Example [귓속말] " + sender + ": " + txt + "\n");
                } else if (msg.mode == ChatMsg.MODE_TX_ORDER) {
                    txtLog.append("🍔 [주문알림] " + sender + "님이 " + txt + "을(를) 주문함.\n");
                }

                // 자동 스크롤
                txtLog.setCaretPosition(txtLog.getDocument().getLength());
            }
        } catch (Exception e) {
            txtLog.append("❌ 서버 연결 종료됨.\n");
        }
    }

    public static void main(String[] args) {
        new SimpleTestClient();
    }
}