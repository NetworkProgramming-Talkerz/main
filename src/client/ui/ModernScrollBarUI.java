package client.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class ModernScrollBarUI extends BasicScrollBarUI {

    @Override
    protected void configureScrollBarColors() {
        this.thumbColor = new Color(100, 100, 100);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private JButton createZeroButton() {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(0, 0));
        btn.setMinimumSize(new Dimension(0, 0));
        btn.setMaximumSize(new Dimension(0, 0));
        return btn;
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(thumbColor);

        // 가로/세로 방향에 따라 패딩을 다르게 적용
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            // 세로: 좌우 여백을 둠
            g2.fillRoundRect(thumbBounds.x + 4, thumbBounds.y,
                    thumbBounds.width - 8, thumbBounds.height, 10, 10);
        } else {
            // 가로: 상하 여백을 둠
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y + 4,
                    thumbBounds.width, thumbBounds.height - 8, 10, 10);
        }
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(new Color(22, 22, 26)); // 배경색과 일치
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }
}