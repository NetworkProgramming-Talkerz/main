package client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {

    private final Color normalColor;
    private final Color hoverColor;

    // 기본 파란색 버튼
    public ModernButton(String text) {
        this(text, new Color(0, 120, 255));
    }

    // 커스텀 색상 버튼
    public ModernButton(String text, Color color) {
        super(text);
        this.normalColor = color;
        this.hoverColor = color.darker();
        decorate();
    }

    private void decorate() {
        setBackground(normalColor);
        setForeground(Color.WHITE);
        setFont(new Font("맑은 고딕", Font.BOLD, 13));

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false); // 커스텀 페인팅을 위해 false
        setCursor(new Cursor(Cursor.HAND_CURSOR));

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

        // 1. 배경 그리기 (둥근 사각형)
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

        // 2. 텍스트 및 아이콘 그리기 (부모 호출)
        super.paintComponent(g);
    }
}