package client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {

    private Color normalColor;
    private Color hoverColor;

    public ModernButton(String text) {
        super(text);

        // 🎨 기본 색상 설정 (PC방 테마에 맞는 파란색)
        this.normalColor = new Color(0, 120, 255);
        this.hoverColor = new Color(0, 100, 220); // 마우스 올렸을 때 조금 진하게

        decorate();
    }

    // 색상을 다르게 쓰고 싶을 때 호출하는 생성자 (예: 회색 버튼)
    public ModernButton(String text, Color color) {
        super(text);
        this.normalColor = color;
        this.hoverColor = color.darker(); // 자동으로 어두운 색 계산

        decorate();
    }

    private void decorate() {
        setBackground(normalColor);
        setForeground(Color.WHITE);
        setFont(new Font("맑은 고딕", Font.BOLD, 13));

        setFocusPainted(false); // 포커스 테두리(점선) 제거
        setBorderPainted(false); // 3D 입체 테두리 제거
        setContentAreaFilled(false); // 기본 배경 채우기 끔 (paintComponent에서 직접 그림)
        setCursor(new Cursor(Cursor.HAND_CURSOR)); // 마우스 올리면 손가락 모양

        // 마우스 호버 이벤트
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(normalColor);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 둥근 사각형 배경 그리기
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // 10은 둥근 정도(Radius)

        super.paintComponent(g);
    }
}
