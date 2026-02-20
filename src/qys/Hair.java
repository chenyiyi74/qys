// java
package qys;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Hair {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("莎总宝录");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            NetworkService net = new NetworkService();

            // 顶部网络状态
            JLabel netStatus = new JLabel("网络状态：检测中...");
            netStatus.setForeground(Color.DARK_GRAY);
            netStatus.setFont(netStatus.getFont().deriveFont(Font.PLAIN, 14f));

            // 中央显示情话的区域
            JLabel quoteLabel = new JLabel("<html><div style='text-align:center;color:#333;'>获得宝录内容</div></html>", SwingConstants.CENTER);
            quoteLabel.setFont(quoteLabel.getFont().deriveFont(Font.PLAIN, 16f));

            // 简化按钮：传入 network service 与显示标签
            RoundedButton centerButton = new RoundedButton("获得宝录内容", net, quoteLabel);
            centerButton.setPreferredSize(new Dimension(220, 48));

            // 顶部面板（网络状态）
            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
            top.add(netStatus, BorderLayout.WEST);

            // 中央布局，按钮在上，情话显示在下
            JPanel center = new JPanel(new GridBagLayout()) {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(420, 160);
                }
            };
            center.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(6, 6, 6, 6);
            center.add(centerButton, gbc);

            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(12, 24, 6, 24);
            center.add(quoteLabel, gbc);

            // 使用自定义背景面板
            BackgroundPanel content = new BackgroundPanel();
            content.setLayout(new BorderLayout());
            content.add(top, BorderLayout.NORTH);
            content.add(center, BorderLayout.CENTER);

            frame.setContentPane(content);

            // 启动网络监控，更新 UI
            net.startMonitoring(online -> SwingUtilities.invokeLater(() -> {
                if (online) {
                    netStatus.setText("网络状态：在线");
                    netStatus.setForeground(new Color(0, 120, 60));
                } else {
                    netStatus.setText("网络状态：离线");
                    netStatus.setForeground(Color.RED);
                }
                centerButton.setNetworkAvailable(online);
            }));

            frame.setSize(480, 280);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // 关闭时停止后台线程
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    net.shutdown();
                }
            });
        });
    }

    // 轻量背景：竖向渐变 + 轻微纹理（简单实现）
    static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, new Color(0xFFF7F9), 0, h, new Color(0xE6F7FF));
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            // 叠加一层非常浅的噪点矩阵感（简易实现）
            g2.setColor(new Color(255, 255, 255, 30));
            for (int y = 0; y < h; y += 18) {
                for (int x = (y / 18) % 2 == 0 ? 0 : 9; x < w; x += 18) {
                    g2.fillOval(x, y, 2, 2);
                }
            }
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }
}