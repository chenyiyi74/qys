// java
package qys;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {
    private static final int RADIUS = 14;
    private final NetworkService networkService;
    private volatile boolean networkAvailable = false;
    private final JLabel outputLabel;

    public RoundedButton(String text, NetworkService networkService, JLabel outputLabel) {
        super(text);
        this.networkService = networkService;
        this.outputLabel = outputLabel;

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        setForeground(Color.WHITE);
        setFont(getFont().deriveFont(Font.BOLD, 15f));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 点击后在 outputLabel 上显示结果（不使用弹窗）
        addActionListener(e -> {
            if (!networkAvailable) {
                outputLabel.setText("<html><div style='text-align:center;color:#a00;'>当前无网络，无法获取</div></html>");
                return;
            }
            setEnabled(false);
            outputLabel.setText("<html><div style='text-align:center;color:#666;'>正在获取宝录语句</div></html>");
            networkService.fetchQuoteAsync(result -> SwingUtilities.invokeLater(() -> {
                if (result == null || result.isEmpty()) {
                    outputLabel.setText("<html><div style='text-align:center;color:#a00;'>获取失败，请稍后重试</div></html>");
                } else {
                    // 自动换行并居中显示
                    String html = "<html><div style='text-align:center;color:#222;'>" + escapeHtml(result).replace("\n", "<br/>") + "</div></html>";
                    outputLabel.setText(html);
                }
                setEnabled(networkAvailable);
            }));
        });

        setEnabled(false); // 初始由网络状态控制
    }

    public void setNetworkAvailable(boolean available) {
        this.networkAvailable = available;
        setEnabled(available);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color fill = isEnabled() ? new Color(0xFF6B6B) : new Color(0xCCCCCC);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w, h, RADIUS, RADIUS);

        // 白色文本水平垂直居中
        g2.dispose();

        super.paintComponent(g);
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    // 简单 HTML 转义，避免破坏标签
    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}