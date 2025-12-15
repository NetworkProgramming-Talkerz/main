package client.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class BubbleMessage extends JPanel {

    private final String text;
    private final boolean isMine;
    private final String time;

    // 텍스트 전용 생성자 (이미지 로직 제거됨)
    public BubbleMessage(String text, boolean isMine) {
        this.text = text;
        this.isMine = isMine;

        // 시간 포맷 통일 (예: 오후 8:15)
        this.time = LocalTime.now()
                .format(DateTimeFormatter.ofPattern("a h:mm"))
                .replace("AM", "오전")
                .replace("PM", "오후");

        setOpaque(false); // 배경 투명 (커스텀 페인팅 위해)

        // 폰트 설정 (여기서 설정해야 FontMetrics 계산이 정확함)
        setFont(new Font("맑은 고딕", Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // 텍스트 품질 향상 (안티앨리어싱)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. 사이즈 및 폰트 측정
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();

        int paddingH = 12; // 말풍선 내부 좌우 여백
        int paddingV = 10; // 말풍선 내부 상하 여백

        int textWidth = fm.stringWidth(text);
        int bubbleWidth = textWidth + (paddingH * 2);
        int bubbleHeight = fm.getHeight() + (paddingV * 2);

        // 시간 폰트 설정
        Font timeFont = new Font("맑은 고딕", Font.PLAIN, 10);
        g2.setFont(timeFont);
        FontMetrics timeFm = g2.getFontMetrics();
        int timeWidth = timeFm.stringWidth(time);

        int gap = 5; // 말풍선과 시간 사이 간격

        // 2. 위치 계산
        int bubbleX, timeX;

        // 시간은 말풍선 바닥 라인에 맞춤
        int timeY = bubbleHeight - 2;

        if (isMine) {
            // [나] : (시간) [말풍선]  <-- 오른쪽 정렬 느낌
            // 부모 패널(FlowLayout)이 RIGHT 정렬이므로, 여기선 상대 좌표만 잡으면 됨
            timeX = 0;
            bubbleX = timeWidth + gap;
        } else {
            // [상대] : [말풍선] (시간)
            bubbleX = 0;
            timeX = bubbleWidth + gap;
        }

        // 3. 말풍선 그리기 (둥근 사각형)
        if (isMine) {
            g2.setColor(new Color(0, 120, 255)); // 파란색
        } else {
            g2.setColor(new Color(60, 60, 60));  // 짙은 회색
        }
        g2.fillRoundRect(bubbleX, 0, bubbleWidth, bubbleHeight, 20, 20);

        // 4. 텍스트 그리기
        g2.setColor(Color.WHITE);
        g2.setFont(getFont()); // 원래 폰트 복구

        // 텍스트 수직 중앙 정렬 보정
        // (Ascent: 기준선에서 글자 위쪽 끝까지의 거리)
        int textY = paddingV + fm.getAscent();
        g2.drawString(text, bubbleX + paddingH, textY);

        // 5. 시간 그리기
        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(timeFont);
        g2.drawString(time, timeX, timeY);
    }

    @Override
    public Dimension getPreferredSize() {
        // paintComponent와 동일한 로직으로 크기 계산해야 짤리지 않음
        FontMetrics fm = getFontMetrics(getFont());
        FontMetrics timeFm = getFontMetrics(new Font("맑은 고딕", Font.PLAIN, 10));

        int paddingH = 12;
        int paddingV = 10;
        int gap = 5;

        int bubbleW = fm.stringWidth(text) + (paddingH * 2);
        int timeW = timeFm.stringWidth(time);

        int totalWidth = bubbleW + timeW + gap;
        int totalHeight = fm.getHeight() + (paddingV * 2);

        return new Dimension(totalWidth, totalHeight);
    }
}