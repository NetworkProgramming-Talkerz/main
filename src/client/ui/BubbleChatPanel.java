package client.ui;

import javax.swing.*;
import java.awt.*;

public class BubbleChatPanel extends JPanel {

    private final JPanel listPanel;

    public BubbleChatPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(22, 22, 26));

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(22, 22, 26));

        // 스크롤이 필요하므로 listPanel 자체는 상단 정렬처럼 보이게
        add(listPanel, BorderLayout.NORTH);
    }

    // ================= 텍스트 말풍선 =================
    public void addBubble(String text, boolean isMine) {
        BubbleMessage bubble = new BubbleMessage(text, isMine);
        addWrapper(bubble, isMine);
    }

    // ================= 이미지 말풍선 =================
    public void addImageBubble(byte[] imgData, boolean isMine) {
        if (imgData == null || imgData.length == 0) return;

        // 1. 이미지 리사이징 (최대 폭 200px 유지)
        ImageIcon originalIcon = new ImageIcon(imgData);
        Image rawImg = originalIcon.getImage();

        int maxWidth = 200;
        int width = originalIcon.getIconWidth();
        int height = originalIcon.getIconHeight();

        if (width > maxWidth) {
            double ratio = (double) width / height;
            width = maxWidth;
            height = (int) (width / ratio);
        }
        Image finalImg = rawImg.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        final int finalW = width;
        final int finalH = height;

        // 2. 둥근 모서리 이미지 패널 생성
        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // super.paintComponent(g); // 투명 배경을 위해 호출하지 않음
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 둥근 클리핑 영역 설정
                java.awt.geom.RoundRectangle2D rounded =
                        new java.awt.geom.RoundRectangle2D.Double(0, 0, finalW, finalH, 30, 30);

                g2.setClip(rounded);
                g2.drawImage(finalImg, 0, 0, finalW, finalH, this);

                // 외곽선 (선택사항)
                g2.setClip(null);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawRoundRect(0, 0, finalW - 1, finalH - 1, 30, 30);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(finalW, finalH);
            }
        };
        imagePanel.setOpaque(false);

        // 3. 시간 표시
        String timeStr = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("a h:mm"))
                .replace("AM", "오전")
                .replace("PM", "오후");
        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        timeLabel.setForeground(Color.LIGHT_GRAY);

        // 4. 배치 (이미지 + 시간)
        JPanel bubbleBox = new JPanel();
        bubbleBox.setLayout(new BoxLayout(bubbleBox, BoxLayout.X_AXIS));
        bubbleBox.setOpaque(false);

        // 하단 정렬
        imagePanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        timeLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        if (isMine) {
            bubbleBox.add(timeLabel);
            bubbleBox.add(Box.createHorizontalStrut(5));
            bubbleBox.add(imagePanel);
        } else {
            bubbleBox.add(imagePanel);
            bubbleBox.add(Box.createHorizontalStrut(5));
            bubbleBox.add(timeLabel);
        }

        // 5. 전체 래퍼에 추가
        addWrapper(bubbleBox, isMine);
    }

    // 공통 래퍼 및 스크롤 처리
    private void addWrapper(Component content, boolean isMine) {
        JPanel wrapper = new JPanel(new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        wrapper.add(content);

        listPanel.add(wrapper);
        listPanel.revalidate();
        listPanel.repaint();
        autoScroll();
    }

    private void autoScroll() {
        SwingUtilities.invokeLater(() -> {
            try {
                JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
                if (scrollPane != null) {
                    JScrollBar vertical = scrollPane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                }
            } catch (Exception ignored) {}
        });
    }
}