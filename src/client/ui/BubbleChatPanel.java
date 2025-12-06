package client.ui;

import javax.swing.*;
import java.awt.*;

public class BubbleChatPanel extends JPanel {

    private final JPanel listPanel; // 말풍선들이 실제로 쌓이는 내부 패널

    public BubbleChatPanel() {
        // 1. 전체 레이아웃을 BorderLayout으로 변경
        setLayout(new BorderLayout());
        setBackground(new Color(22, 22, 26));

        // 2. 말풍선을 담을 리스트 패널 생성 (세로로 쌓이는 BoxLayout)
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(22, 22, 26));

        // 3. 리스트 패널을 NORTH(북쪽/상단)에 배치
        // -> 이렇게 하면 메시지가 적을 때도 위쪽에 딱 붙어서 나옵니다.
        add(listPanel, BorderLayout.NORTH);
    }

    public void addBubble(String text, boolean isMine) {
        BubbleMessage bubble = new BubbleMessage(text, isMine);

        // 좌우 정렬을 위한 래퍼(Wrapper) 패널
        JPanel wrapper = new JPanel(new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.add(bubble);

        // 4. 메인 패널이 아니라 내부 listPanel에 추가해야 함
        listPanel.add(wrapper);

        // 화면 갱신
        listPanel.revalidate();
        listPanel.repaint();

        // 5. ✨ 자동 스크롤 기능 (가장 중요!)
        // 화면이 그려진 직후(invokeLater) 스크롤을 맨 아래로 내림
        SwingUtilities.invokeLater(() -> {
            try {
                // 내 부모 중에 JScrollPane이 있는지 찾습니다.
                JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);

                if (scrollPane != null) {
                    JScrollBar vertical = scrollPane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum()); // 스크롤바를 최대값(맨 아래)으로 이동
                }
            } catch (Exception e) {
                // 스크롤바를 못 찾아도 에러 없이 무시
            }
        });
    }
}

/*package client.ui;

import javax.swing.*;
import java.awt.*;

public class BubbleChatPanel extends JPanel {

    public BubbleChatPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(25, 25, 30));
    }

    public void addBubble(String text, boolean isMine) {
        BubbleMessage bubble = new BubbleMessage(text, isMine);

        JPanel wrapper = new JPanel(new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.add(bubble);

        add(wrapper);
        revalidate();
        repaint();
    }
}
*/