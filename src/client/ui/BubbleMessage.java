package client.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class BubbleMessage extends JPanel {

    private final String text;
    private final boolean isMine;
    private final String time; // 시간 문자열 저장

    public BubbleMessage(String text, boolean isMine) {
        this.text = text;
        this.isMine = isMine;

        // ⏰ 현재 시간을 "오후 8:15" 형식으로 자동 생성
        this.time = LocalTime.now().format(DateTimeFormatter.ofPattern("a h:mm"));

        setOpaque(false);
        setFont(new Font("맑은 고딕", Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. 폰트 및 사이즈 측정
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int paddingH = 12; // 말풍선 내부 좌우 여백
        int paddingV = 10; // 말풍선 내부 상하 여백

        int bubbleWidth = fm.stringWidth(text) + (paddingH * 2);
        int bubbleHeight = fm.getHeight() + (paddingV * 2);

        // 시간 폰트 설정 (작고 회색)
        Font timeFont = new Font("맑은 고딕", Font.PLAIN, 10);
        g2.setFont(timeFont);
        FontMetrics timeFm = g2.getFontMetrics();
        int timeWidth = timeFm.stringWidth(time);

        // 간격 (말풍선과 시간 사이)
        int gap = 5;

        // 2. 좌표 계산 (핵심!)
        int bubbleX, timeX;

        // 텍스트(말풍선 내용) Y좌표 (수직 중앙)
        int textY = (bubbleHeight - fm.getHeight()) / 2 + fm.getAscent() + paddingV;

        // 시간 Y좌표 (말풍선 바닥 라인에 맞춤)
        int timeY = bubbleHeight - 2;

        if (isMine) {
            // [나] : (시간) (말풍선) 순서
            timeX = 0;
            bubbleX = timeWidth + gap;
        } else {
            // [남] : (말풍선) (시간) 순서
            bubbleX = 0;
            timeX = bubbleWidth + gap;
        }

        // 3. 말풍선 그리기 (배경)
        if (isMine) {
            g2.setColor(new Color(0, 120, 255));
        } else {
            g2.setColor(new Color(60, 60, 60));
        }
        // 좌표(bubbleX)를 적용해서 그립니다.
        g2.fillRoundRect(bubbleX, 0, bubbleWidth, bubbleHeight, 15, 15);

        // 4. 말풍선 내용(텍스트) 그리기
        g2.setColor(Color.WHITE);
        g2.setFont(getFont()); // 원래 폰트 복구
        // 좌표에 paddingH 만큼 더해서 내부 여백 확보
        g2.drawString(text, bubbleX + paddingH, paddingV + fm.getAscent());

        // 5. 시간 그리기
        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(timeFont);
        g2.drawString(time, timeX, timeY);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        FontMetrics timeFm = getFontMetrics(new Font("맑은 고딕", Font.PLAIN, 10));

        int paddingH = 24; // 말풍선 내부 여백 합
        int paddingV = 20;
        int gap = 5; // 말풍선과 시간 사이 간격

        int bubbleW = fm.stringWidth(text) + paddingH;
        int timeW = timeFm.stringWidth(time);

        // 전체 가로 길이 = 말풍선 길이 + 시간 길이 + 간격
        int totalWidth = bubbleW + timeW + gap;
        int totalHeight = fm.getHeight() + paddingV;

        return new Dimension(totalWidth, totalHeight);
    }
}