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
import java.util.HashMap;
import java.util.Map;

public class StaffClient extends JFrame {

    private ClientConnection conn = new ClientConnection();
    private BubbleChatPanel chatPanel = new BubbleChatPanel();
    private JTextField inputField = new JTextField();
    private JTextArea orderArea = new JTextArea();
    private JPanel orderListPanel = new JPanel();

    // 접속 중인 PC 리스트(드롭다운)
    private final DefaultComboBoxModel<String> pcComboModel = new DefaultComboBoxModel<>();
    private JComboBox<String> pcCombo;

    // PC별 남은 시간 저장
    private final Map<String, Integer> remainTimeMap = new HashMap<>();
    private JLabel remainLabel;

    // 시간 추가 입력
    private JTextField addHourField;

    public void start() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader("server.txt"))) {
            String ip = br.readLine();
            int port = Integer.parseInt(br.readLine());
            conn.connect(ip, port);
        }

        buildUI();

        // STAFF 접속 알림 (서버에서 CONNECT_NOTICE 생성)
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
        orderArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));


        JScrollPane chatScroll = new JScrollPane(chatPanel);
        chatScroll.setBorder(null);
        chatScroll.getVerticalScrollBar().setUI(new client.ui.ModernScrollBarUI());
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        chatScroll.getViewport().setBackground(new Color(22, 22, 26));

        orderListPanel.setLayout(new BoxLayout(orderListPanel, BoxLayout.Y_AXIS));
        orderListPanel.setBackground(new Color(22, 22, 26));

        JScrollPane orderScroll = new JScrollPane(orderListPanel);

        javax.swing.border.TitledBorder orderBorder =
                BorderFactory.createTitledBorder("주문 내역");
        orderBorder.setTitleColor(Color.WHITE); // 제목 하얀색
        orderBorder.setTitleFont(new Font("맑은 고딕", Font.BOLD, 14));

        orderScroll.setBorder(orderBorder);
        orderScroll.setBackground(new Color(22, 22, 26));
        orderScroll.getViewport().setBackground(new Color(22, 22, 26));
        orderScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
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

        // ─── 상단: 대상 PC 드롭다운 + 잔여 시간 + 시간추가/강제종료 + 전체 공지 ───
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(30, 30, 35));

        JLabel lbl = new JLabel("대상 PC:");
        lbl.setForeground(Color.white);

        pcCombo = new JComboBox<>(pcComboModel);
        pcCombo.setPreferredSize(new Dimension(80, 26));
        pcCombo.addActionListener(e -> updateRemainLabel());

        remainLabel = new JLabel("잔여 시간: - 분");
        remainLabel.setForeground(new Color(148, 163, 184));
        remainLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        addHourField = new JTextField("1", 3);
        addHourField.setBackground(new Color(45, 45, 55));
        addHourField.setForeground(Color.white);

        ModernButton addTimeBtn = new ModernButton("+시간");
        ModernButton kickBtn = new ModernButton("강제 종료", new Color(220, 80, 80));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.setOpaque(false);
        left.add(lbl);
        left.add(pcCombo);
        left.add(remainLabel);

        JLabel addTimeLabel = new JLabel("  추가(시간):");
        addTimeLabel.setForeground(Color.WHITE);
        left.add(addTimeLabel);

        left.add(addHourField);
        left.add(addTimeBtn);
        left.add(kickBtn);

        ModernButton noticeBtn = new ModernButton("전체 공지", new Color(220, 50, 50));
        top.add(left, BorderLayout.WEST);
        top.add(noticeBtn, BorderLayout.EAST);

        // ─── 하단: 입력창 + 보내기 버튼 ───
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(30, 30, 35));

        inputField.setBackground(new Color(45, 45, 55));
        inputField.setForeground(Color.white);

        ModernButton sendBtn  = new ModernButton("보내기");
        ModernButton emojiBtn = new ModernButton("😊", new Color(60, 60, 70));
        emojiBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        bottom.add(inputField, BorderLayout.CENTER);

        JPanel rightBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBtn.setOpaque(false);
        rightBtn.add(sendBtn);
        rightBtn.add(emojiBtn);

        bottom.add(rightBtn, BorderLayout.EAST);


        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> sendPrivate());
        inputField.addActionListener(e -> sendPrivate());
        noticeBtn.addActionListener(e -> sendGroup());
        addTimeBtn.addActionListener(e -> addTimeForSelectedPc());
        kickBtn.addActionListener(e -> forceLogoutSelectedPc());


        emojiBtn.addActionListener(e -> selectAndSendImage());

        inputField.setBackground(new Color(45, 45, 55));
        inputField.setForeground(Color.white);
        inputField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
// 👇 [추가] 배경이 어두우니 커서를 '흰색'으로 변경
        inputField.setCaretColor(Color.WHITE);
    }

    // ───────────────── 전송 로직 ─────────────────

    // [수정] 직원용 이모티콘 전송
    private void selectAndSendImage() {
        String pc = (String) pcCombo.getSelectedItem();
        if (pc == null || pc.isBlank()) {
            JOptionPane.showMessageDialog(this, "대상 PC를 먼저 선택하세요.");
            return;
        }

        // 이모티콘 선택창 띄우기
        new EmoticonPicker(this, (imgData) -> {
            try {
                // 1. 서버로 전송
                conn.sendMessage(new Message(
                        Message.Mode.EMOJI,
                        "STAFF",
                        pc,
                        "[이모티콘]",
                        imgData
                ));

                // 2. 내 채팅창에도 표시
                chatPanel.addImageBubble(imgData, true);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "전송 실패: " + ex.getMessage());
            }
        }).setVisible(true);
    }


    private void sendPrivate() {
        String t = inputField.getText().trim();
        if (t.isEmpty()) return;

        String pc = (String) pcCombo.getSelectedItem();
        if (pc == null || pc.isBlank()) {
            JOptionPane.showMessageDialog(this, "대상 PC를 먼저 선택하세요.");
            return;
        }

        conn.sendMessage(new Message(
                Message.Mode.PRIVATE_CHAT,
                "STAFF",
                pc,
                t,
                null
        ));
        chatPanel.addBubble("STAFF: " + t, true);
        inputField.setText("");
    }

    private void sendGroup() {
        String text = JOptionPane.showInputDialog(this, "공지 내용");
        if (text == null || text.isBlank()) return;
        conn.sendMessage(new Message(
                Message.Mode.GROUP_CHAT,
                "STAFF",
                "ALL",
                text,
                null
        ));
        chatPanel.addBubble("[공지] " + text, true);
    }


    // 선택한 PC에 시간 추가 (시간 단위)
    private void addTimeForSelectedPc() {
        String pc = (String) pcCombo.getSelectedItem();
        if (pc == null) {
            JOptionPane.showMessageDialog(this, "대상 PC를 먼저 선택하세요.");
            return;
        }

        String text = addHourField.getText().trim();
        int hours;
        try {
            hours = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "추가 시간을 '정수 시간' 단위로 입력해 주세요.");
            return;
        }
        if (hours == 0) return;

        int minutes = hours * 60;

        int r = JOptionPane.showConfirmDialog(
                this,
                pc + " 에 " + hours + "시간을 추가하시겠습니까?",
                "시간 추가 확인",
                JOptionPane.YES_NO_OPTION
        );

        if (r != JOptionPane.YES_OPTION) return;

        conn.sendMessage(new Message(
                Message.Mode.ADD_TIME,
                "STAFF",
                pc,                     // receiver = 대상 PC
                "시간 추가",
                new TimeInfo(minutes)   // 추가할 분
        ));
    }

    // 선택한 PC 강제 종료
    private void forceLogoutSelectedPc() {
        String pc = (String) pcCombo.getSelectedItem();
        if (pc == null) {
            JOptionPane.showMessageDialog(this, "대상 PC를 먼저 선택하세요.");
            return;
        }

        int r = JOptionPane.showConfirmDialog(
                this,
                pc + " 을(를) 강제 종료하시겠습니까?",
                "강제 종료",
                JOptionPane.YES_NO_OPTION
        );
        if (r != JOptionPane.YES_OPTION) return;

        conn.sendMessage(new Message(
                Message.Mode.FORCE_LOGOUT,
                "STAFF",
                pc,
                "강제 종료",
                null
        ));
    }

    // ───────────────── 수신 처리 ─────────────────

    private void handleIncoming(Message msg) {
        switch (msg.getMode()) {
            case CONNECT_NOTICE -> {
                chatPanel.addBubble("[공지] " + msg.getContent(), false);
                handleConnectNotice(msg);
            }

            case TIME_UPDATE -> {
                handleTimeUpdate(msg);
            }

            case PRIVATE_CHAT,GROUP_CHAT, CHAT ->
                    chatPanel.addBubble(msg.getSender() + ": " + msg.getContent(), false);

            case ORDER -> {
                String pcNum = msg.getSender();
                String summary = msg.getContent();

                chatPanel.addBubble(pcNum + ": " + summary, false);

                if (msg.getData() instanceof Order o) {

                    String time = new java.text.SimpleDateFormat("HH:mm:ss")
                            .format(new java.util.Date(o.getTimestamp()));

                    String[] menuList = o.getMenuName().split(", ");

                    // 주문 제목: [01번 주문]
                    String orderTitle =
                            summary.replaceAll("\\s*\\(.*?\\)", "")
                                    .replaceAll("\\[주문\\]\\s*", "");

                    // 총액 추출
                    String totalPrice =
                            summary.replaceAll(".*\\(총\\s*", "")
                                    .replaceAll("\\).*", "");

                    // ===== 주문 카드 생성 =====
                    JPanel card = new JPanel(new BorderLayout());
                    card.setBackground(new Color(30, 30, 35));
                    card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

                    // ── 상단 (헤더 + 버튼)
                    JPanel top = new JPanel(new BorderLayout());
                    top.setOpaque(false);

                    JLabel header = new JLabel("█ " + pcNum + " [" + orderTitle + "]");
                    header.setForeground(Color.WHITE);
                    header.setFont(new Font("맑은 고딕", Font.BOLD, 14));

                    ModernButton confirmBtn = new ModernButton("주문 확인");
                    confirmBtn.addActionListener(e -> {
                        conn.sendMessage(new Message(
                                Message.Mode.PRIVATE_CHAT,
                                "STAFF",
                                pcNum,
                                "[주문] " + orderTitle + " 준비 중입니다",
                                null
                        ));
                        confirmBtn.setEnabled(false);
                        confirmBtn.setText("확인됨");
                    });

                    top.add(header, BorderLayout.WEST);
                    top.add(confirmBtn, BorderLayout.EAST);

                    // ── 메뉴 목록
                    JPanel menuPanel = new JPanel();
                    menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
                    menuPanel.setOpaque(false);

                    for (String menu : menuList) {
                        JLabel m = new JLabel("   - " + menu);
                        m.setForeground(Color.LIGHT_GRAY);
                        menuPanel.add(m);
                    }

                    // ── 하단 (합계 + 시간)
                    JLabel footer = new JLabel("합계 " + totalPrice + "        " + time);
                    footer.setForeground(new Color(180, 180, 180));

                    card.add(top, BorderLayout.NORTH);
                    card.add(menuPanel, BorderLayout.CENTER);
                    card.add(footer, BorderLayout.SOUTH);

                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));

                    // 카드 추가
                    orderListPanel.add(card);
                    orderListPanel.add(Box.createVerticalStrut(10));
                    orderListPanel.revalidate();
                    orderListPanel.repaint();
                    }

                else {
                    orderArea.append("🚨 주문 오류: 데이터 없음\n");
                }

                orderArea.setCaretPosition(orderArea.getDocument().getLength());
            }

            case EMOJI -> {
                // data 필드에 byte[]가 들어있는지 확인
                if (msg.getData() instanceof byte[] imgData) {
                    chatPanel.addImageBubble(imgData, false);
                }
            }
        }
    }

    // ───────────────── 접속/퇴장 공지 처리 ─────────────────

    private void handleConnectNotice(Message msg) {
        // 예: "PC01 접속", "PC01 퇴장", "STAFF 접속"
        String content = msg.getContent();
        if (content == null) return;

        // 맨 앞 토큰을 PC번호로 가정
        String pc;
        int idx = content.indexOf(' ');
        if (idx > 0) {
            pc = content.substring(0, idx).trim();
        } else {
            pc = content.trim();
        }

        // STAFF 자기 자신은 목록에 넣지 않음
        if ("STAFF".equals(pc)) return;

        if (content.contains("접속")) {
            if (!containsPc(pc)) {
                pcComboModel.addElement(pc);
                if (pcCombo.getSelectedItem() == null) {
                    pcCombo.setSelectedItem(pc);
                }
            }
        } else if (content.contains("퇴장")) {
            removePc(pc);
        }
    }

    private boolean containsPc(String pc) {
        for (int i = 0; i < pcComboModel.getSize(); i++) {
            if (pc.equals(pcComboModel.getElementAt(i))) return true;
        }
        return false;
    }

    private void removePc(String pc) {
        // 드롭다운에서 제거
        for (int i = 0; i < pcComboModel.getSize(); i++) {
            if (pc.equals(pcComboModel.getElementAt(i))) {
                pcComboModel.removeElementAt(i);
                break;
            }
        }
        // 시간 정보 삭제
        remainTimeMap.remove(pc);

        // 현재 선택된 PC였으면 선택/라벨 초기화
        String selected = (String) pcCombo.getSelectedItem();
        if (pc.equals(selected)) {
            if (pcComboModel.getSize() > 0) {
                pcCombo.setSelectedIndex(0);
            } else {
                pcCombo.setSelectedItem(null);
                remainLabel.setText("잔여 시간: - 분");
            }
        } else {
            updateRemainLabel();
        }
    }

    // ───────────────── TIME_UPDATE 처리 ─────────────────

    private void handleTimeUpdate(Message msg) {
        // STAFF에게 온 TIME_UPDATE만 처리
        if (!"STAFF".equals(msg.getReceiver())) return;
        if (!(msg.getData() instanceof TimeInfo ti)) return;

        // 서버에서 sender = PC번호라고 가정
        String pc = msg.getSender();
        int remain = ti.getRemainingMinutes();

        remainTimeMap.put(pc, remain);

        // 드롭다운에 없으면 추가
        if (!containsPc(pc)) {
            pcComboModel.addElement(pc);
            if (pcCombo.getSelectedItem() == null) {
                pcCombo.setSelectedItem(pc);
            }
        }

        updateRemainLabel();
    }

    private void updateRemainLabel() {
        if (remainLabel == null) return;
        String selected = (String) pcCombo.getSelectedItem();
        if (selected == null) {
            remainLabel.setText("잔여 시간: - 분");
            return;
        }
        Integer remain = remainTimeMap.get(selected);
        if (remain == null) {
            remainLabel.setText("잔여 시간: - 분");
        } else {
            remainLabel.setText("잔여 시간: " + remain + "분");
        }
    }
}
