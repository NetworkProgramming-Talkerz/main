package client.ui;

import javax.swing.*;
import java.awt.*;

public class BalloonPanel extends JPanel {

    private String text;
    private boolean isMine;

    public BalloonPanel(String text, boolean isMine) {
        this.text = text;
        this.isMine = isMine;

        setOpaque(false);
        setLayout(new BorderLayout());

        JLabel label = new JLabel("<html>" + text.replace("\n", "<br>") + "</html>");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.add(label);

        if (isMine) {
            add(textPanel, BorderLayout.EAST);
        } else {
            add(textPanel, BorderLayout.WEST);
        }

        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int arc = 15;

        if (isMine) {
            g.setColor(new Color(0, 122, 255));
            g.fillRoundRect(getWidth() - 250, 10, 230, getHeight() - 20, arc, arc);
        } else {
            g.setColor(new Color(60, 60, 60));
            g.fillRoundRect(10, 10, 230, getHeight() - 20, arc, arc);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 60);
    }
}
