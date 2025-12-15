package client.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class OrderDialog extends JDialog {

    // 장바구니 아이템 DTO
    private static class OrderItem {
        String name;
        int price;
        int quantity;

        public OrderItem(String name, int price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
    }

    private final BiConsumer<String, Integer> onOrderAction; // 주문 콜백
    private final List<OrderItem> cart = new ArrayList<>();

    // UI 컴포넌트
    private final JPanel cartListPanel;
    private final JLabel totalPriceLabel;
    private final ModernButton orderBtn;

    public OrderDialog(JFrame parent, BiConsumer<String, Integer> onOrderAction) {
        super(parent, "상품 주문", true); // 모달 창
        this.onOrderAction = onOrderAction;

        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(22, 22, 26));

        // [메인 패널] 2단 분리 (메뉴판 | 장바구니)
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(new Color(22, 22, 26));

        // 1️⃣ 왼쪽: 메뉴판 구성
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBackground(new Color(22, 22, 26));

        JLabel menuTitle = new JLabel("메뉴 선택", SwingConstants.CENTER);
        menuTitle.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        menuTitle.setForeground(Color.WHITE);
        menuTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        menuPanel.add(menuTitle, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // 2열 그리드
        gridPanel.setBackground(new Color(22, 22, 26));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 메뉴 데이터 등록
        addMenuCard(gridPanel, "짜파게티", 4500, "jjapa.png");
        addMenuCard(gridPanel, "김치볶음밥", 6000, "kimchi.png");
        addMenuCard(gridPanel, "핫도그", 3000, "hotdog.png");
        addMenuCard(gridPanel, "아메리카노", 2500, "coffee.png");
        addMenuCard(gridPanel, "소떡소떡", 3500, "sotteok.png");
        addMenuCard(gridPanel, "치즈라면", 4500, "cheese_ramen.png");
        addMenuCard(gridPanel, "제육덮밥", 7000, "pork_rice.png");
        addMenuCard(gridPanel, "컵라면", 1500, "cup_ramen.png");
        addMenuCard(gridPanel, "콜라", 1500, "coke.png");
        addMenuCard(gridPanel, "사이다", 1500, "cider.png");

        JScrollPane menuScroll = new JScrollPane(gridPanel);
        menuScroll.setBorder(null);
        menuScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        menuScroll.getVerticalScrollBar().setUnitIncrement(16);
        menuScroll.getViewport().setBackground(new Color(22, 22, 26));
        menuPanel.add(menuScroll, BorderLayout.CENTER);

        // 2️⃣ 오른쪽: 장바구니 구성
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBackground(new Color(30, 30, 35));
        cartPanel.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));

        JLabel cartTitle = new JLabel("장바구니", SwingConstants.CENTER);
        cartTitle.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        cartTitle.setForeground(Color.WHITE);
        cartTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        cartPanel.add(cartTitle, BorderLayout.NORTH);

        cartListPanel = new JPanel();
        cartListPanel.setLayout(new BoxLayout(cartListPanel, BoxLayout.Y_AXIS));
        cartListPanel.setBackground(new Color(30, 30, 35));

        JScrollPane cartScroll = new JScrollPane(cartListPanel);
        cartScroll.setBorder(null);
        cartScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        cartScroll.getViewport().setBackground(new Color(30, 30, 35));
        cartPanel.add(cartScroll, BorderLayout.CENTER);

        // 하단: 총 금액 및 주문 버튼
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(40, 40, 45));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        totalPriceLabel = new JLabel("총 주문금액: 0원", SwingConstants.RIGHT);
        totalPriceLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        totalPriceLabel.setForeground(new Color(255, 200, 0));
        totalPriceLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        orderBtn = new ModernButton("주문하기 (0개)");
        orderBtn.addActionListener(e -> processOrder());

        bottomPanel.add(totalPriceLabel, BorderLayout.NORTH);
        bottomPanel.add(orderBtn, BorderLayout.SOUTH);
        cartPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(menuPanel);
        mainPanel.add(cartPanel);
        add(mainPanel, BorderLayout.CENTER);

        // 전체 닫기 버튼
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closePanel.setBackground(new Color(22, 22, 26));
        ModernButton closeBtn = new ModernButton("닫기", new Color(60, 60, 70));
        closeBtn.addActionListener(e -> dispose());
        closePanel.add(closeBtn);
        add(closePanel, BorderLayout.SOUTH);
    }

    // 메뉴 카드 생성 헬퍼 메서드
    private void addMenuCard(JPanel panel, String name, int price, String imgPath) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(35, 35, 40));
        card.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 60), 1));

        // 메뉴 이름
        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        card.add(nameLabel, BorderLayout.NORTH);

        // 메뉴 이미지 로드
        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // src/order_menu 폴더에 이미지가 있다고 가정 (ClassPath 리소스)
        java.net.URL imgUrl = getClass().getResource("/order_menu/" + imgPath);
        if (imgUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imgUrl);
            Image scaledImg = originalIcon.getImage().getScaledInstance(120, 90, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(scaledImg));
        } else {
            imgLabel.setText("NO IMG");
            imgLabel.setForeground(Color.GRAY);
        }
        card.add(imgLabel, BorderLayout.CENTER);

        // 가격 + 담기 버튼
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        bottomPanel.setBackground(new Color(35, 35, 40));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel priceLabel = new JLabel(String.format("%,d원", price));
        priceLabel.setForeground(new Color(255, 255, 255));
        priceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        priceLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        ModernButton addBtn = new ModernButton("담기");
        addBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        addBtn.addActionListener(e -> addToCart(name, price));

        bottomPanel.add(priceLabel);
        bottomPanel.add(addBtn);

        card.add(bottomPanel, BorderLayout.SOUTH);
        panel.add(card);
    }

    private void addToCart(String name, int price) {
        boolean exists = false;
        for (OrderItem item : cart) {
            if (item.name.equals(name)) {
                item.quantity++;
                exists = true;
                break;
            }
        }
        if (!exists) {
            cart.add(new OrderItem(name, price, 1));
        }
        updateCartUI();
    }

    private void updateCartUI() {
        cartListPanel.removeAll();
        int total = 0;
        int count = 0;

        for (OrderItem item : cart) {
            total += (item.price * item.quantity);
            count += item.quantity;

            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            itemPanel.setBackground(new Color(30, 30, 35));
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            JLabel txt = new JLabel(item.name + " x " + item.quantity);
            txt.setForeground(Color.WHITE);
            txt.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

            JLabel price = new JLabel(String.format("%,d원", item.price * item.quantity));
            price.setForeground(Color.LIGHT_GRAY);

            JButton delBtn = new JButton("X");
            delBtn.setBorderPainted(false);
            delBtn.setContentAreaFilled(false);
            delBtn.setForeground(Color.RED);
            delBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            delBtn.addActionListener(e -> {
                cart.remove(item);
                updateCartUI();
            });

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            right.setOpaque(false);
            right.add(price);
            right.add(delBtn);

            itemPanel.add(txt, BorderLayout.WEST);
            itemPanel.add(right, BorderLayout.EAST);
            cartListPanel.add(itemPanel);
        }

        totalPriceLabel.setText(String.format("총 주문금액: %,d원", total));
        orderBtn.setText("주문하기 (" + count + "개)");

        cartListPanel.revalidate();
        cartListPanel.repaint();
    }

    private void processOrder() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "장바구니가 비어있습니다.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        int total = 0;

        for (int i = 0; i < cart.size(); i++) {
            OrderItem item = cart.get(i);
            sb.append(item.name).append(" ").append(item.quantity).append("개");
            if (i < cart.size() - 1) sb.append(", ");
            total += (item.price * item.quantity);
        }

        onOrderAction.accept(sb.toString(), total);
        JOptionPane.showMessageDialog(this, "주문이 접수되었습니다.");
        dispose();
    }
}