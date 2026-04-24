package taskmanager.ui.components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/** Custom dark-themed scrollbar. */
public class DarkScrollBarUI extends BasicScrollBarUI {

    @Override protected void configureScrollBarColors() {
        thumbColor = new Color(55, 65, 90);
        trackColor = Theme.BG_DARK;
    }

    @Override protected JButton createDecreaseButton(int o) { return invisible(); }
    @Override protected JButton createIncreaseButton(int o) { return invisible(); }

    @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if (r.isEmpty()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isDragging ? new Color(80, 95, 130) : thumbColor);
        g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
        g2.dispose();
    }

    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        g.setColor(trackColor);
        g.fillRect(r.x, r.y, r.width, r.height);
    }

    private JButton invisible() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }
}
