package client.ui;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Consumer;

public class EmoticonPicker extends JDialog {

    // 📌 src/images/emoji 폴더 파일명과 일치해야 함
    private final String[] emojiFiles = {
            "smile.png", "sad.png", "question.png",
            "check.png", "thanks.png", "snack.png"
    };

    private final Consumer<byte[]> onSelect;

    public EmoticonPicker(JFrame owner, Consumer<byte[]> onSelect) {
        super(owner, "이모티콘", true);
        this.onSelect = onSelect;

        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(30, 30, 35));

        // 1. 그리드 패널
        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        gridPanel.setBackground(new Color(30, 30, 35));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for (String fileName : emojiFiles) {
            JButton btn = createEmojiButton(fileName);
            if (btn != null) {
                gridPanel.add(btn);
            }
        }

        // 2. 스크롤 설정 (가로 스크롤 방지)
        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        add(scroll, BorderLayout.CENTER);

        // 창 크기 및 위치 설정
        setSize(400, 350);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private JButton createEmojiButton(String fileName) {
        String path = "/emoji/" + fileName;
        java.net.URL imgUrl = getClass().getResource(path);

        if (imgUrl == null) {
            // 이미지가 없을 경우 텍스트 버튼으로 대체 (디버깅용)
            JButton txtBtn = new JButton(fileName);
            txtBtn.setForeground(Color.GRAY);
            txtBtn.setContentAreaFilled(false);
            txtBtn.setBorderPainted(false);
            return txtBtn;
        }

        // 아이콘 리사이징
        ImageIcon icon = new ImageIcon(imgUrl);
        Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
        JButton btn = new JButton(new ImageIcon(img));

        // 버튼 스타일 제거
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            byte[] data = loadResourceBytes(path);
            if (data != null) {
                onSelect.accept(data);
                dispose();
            }
        });

        return btn;
    }

    private byte[] loadResourceBytes(String path) {
        try (InputStream is = getClass().getResourceAsStream(path);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            if (is == null) return null;

            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}