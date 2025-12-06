package common;

import javax.swing.*;
import java.awt.*;

public class CustomUI {

    public static final Color BG = new Color(43, 43, 43);
    public static final Color PANEL = new Color(52, 52, 52);
    public static final Color ACCENT = new Color(77, 163, 255);
    public static final Color TEXT = new Color(230, 230, 230);

    public static final Font FONT_MAIN = new Font("맑은 고딕", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("맑은 고딕", Font.BOLD, 15);

    public static void styleTextArea(JTextArea area) {
        area.setBackground(PANEL);
        area.setForeground(TEXT);
        area.setFont(FONT_MAIN);
        area.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        area.setCaretColor(TEXT);
    }

    public static void styleInput(JTextField field) {
        field.setBackground(new Color(60, 60, 60));
        field.setForeground(TEXT);
        field.setFont(FONT_MAIN);
        field.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90)));
        field.setCaretColor(TEXT);
    }

    public static JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT);
        btn.setForeground(Color.white);
        btn.setFont(FONT_MAIN);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        return btn;
    }

    public static JScrollPane styleScroll(JTextArea area) {
        JScrollPane sp = new JScrollPane(area);
        sp.getViewport().setBackground(PANEL);
        sp.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        return sp;
    }

    public static JPanel stylePanel(JPanel p) {
        p.setBackground(PANEL);
        return p;
    }
}
