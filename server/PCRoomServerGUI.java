package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PCRoomServerGUI extends JFrame {
    private JTextArea textArea;
    private JButton btnStart;
    private PCRoomServer server;

    public PCRoomServerGUI() {
        super("PC방 관리 서버 (Team A)");
        buildGUI();
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void buildGUI() {
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel botPanel = new JPanel();
        btnStart = new JButton("서버 시작");
        btnStart.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        btnStart.setBackground(new Color(100, 200, 100)); // 초록색 버튼

        botPanel.add(btnStart);
        add(botPanel, BorderLayout.SOUTH);

        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 서버 객체 생성 및 시작
                server = new PCRoomServer(54321, PCRoomServerGUI.this);
                new Thread(() -> server.startServer()).start(); // 스레드로 실행

                btnStart.setEnabled(false);
                btnStart.setText("서버 실행 중...");
            }
        });
    }

    public void appendLog(String msg) {
        textArea.append(msg + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        new PCRoomServerGUI();
    }
}