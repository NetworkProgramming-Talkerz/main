package client.ui;

import javax.swing.*;
import java.awt.*;

public class ChatListPanel extends JPanel {


    public ChatListPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(30, 30, 30));
    }

    public void addMessage(String text, boolean isMine) {
        BalloonPanel bp = new BalloonPanel(text, isMine);
        add(bp);
        add(Box.createVerticalStrut(8));
        revalidate();
        repaint();
    }
}

