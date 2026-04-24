package taskmanager.ui.dashboard;

import taskmanager.ui.components.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Animated mini bar chart for the dashboard.
 */
public class MiniBarChart extends JPanel {

    private String[] labels = {};
    private int[]    values = {};
    private Color[]  colors = {};
    private float    animProgress = 0f;

    public MiniBarChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(200, 180));
    }

    public void setData(String[] labels, int[] values, Color[] colors) {
        this.labels = labels;
        this.values = values;
        this.colors = colors;
        animProgress = 0f;
        Timer t = new Timer(16, null);
        t.addActionListener(e -> {
            animProgress = Math.min(1f, animProgress + 0.04f);
            repaint();
            if (animProgress >= 1f) t.stop();
        });
        t.start();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (values.length == 0) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int max = 0;
        for (int v : values) max = Math.max(max, v);
        if (max == 0) max = 1;

        int w = getWidth(), h = getHeight();
        int padL = 8, padR = 8, padT = 10, padB = 30;
        int chartH = h - padT - padB;
        int n = values.length;
        int barW = (w - padL - padR) / n - 6;

        for (int i = 0; i < n; i++) {
            int barH = (int)((float) values[i] / max * chartH * animProgress);
            int x = padL + i * ((w - padL - padR) / n) + 3;
            int y = padT + chartH - barH;

            // Bar
            Color c = colors[i % colors.length];
            GradientPaint gp = new GradientPaint(x, y, c, x, y + barH, Theme.withAlpha(c, 120));
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(x, y, barW, barH, 5, 5));

            // Value label
            if (values[i] > 0) {
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(Theme.FONT_SMALL);
                String vs = String.valueOf(values[i]);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(vs, x + (barW - fm.stringWidth(vs)) / 2, y - 3);
            }

            // X label
            g2.setColor(Theme.TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String lbl = labels[i];
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lbl, x + (barW - fm.stringWidth(lbl)) / 2, h - 10);
        }
        g2.dispose();
    }
}

/**
 * Animated donut/pie chart for status breakdown.
 */
class DonutChart extends JPanel {

    private String[] labels = {};
    private int[]    values = {};
    private Color[]  colors = {};
    private float    animProgress = 0f;
    private int      hoverSlice   = -1;

    public DonutChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(200, 180));
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                // Basic hover detection on center region
                hoverSlice = -1; repaint();
            }
        });
    }

    public void setData(String[] labels, int[] values, Color[] colors) {
        this.labels = labels;
        this.values = values;
        this.colors = colors;
        animProgress = 0f;
        Timer t = new Timer(16, null);
        t.addActionListener(e -> {
            animProgress = Math.min(1f, animProgress + 0.035f);
            repaint();
            if (animProgress >= 1f) t.stop();
        });
        t.start();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (values.length == 0) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int total = 0; for (int v : values) total += v;
        if (total == 0) { g2.setColor(Theme.TEXT_MUTED); g2.setFont(Theme.FONT_SMALL);
            g2.drawString("No data", getWidth()/2 - 20, getHeight()/2); g2.dispose(); return; }

        int cx = getWidth() / 3, cy = getHeight() / 2;
        int r = Math.min(cx, cy) - 10;
        int innerR = (int)(r * 0.55);

        float startAngle = -90f;
        for (int i = 0; i < values.length; i++) {
            float sweep = (float) values[i] / total * 360f * animProgress;
            g2.setColor(colors[i % colors.length]);
            g2.fill(new Arc2D.Float(cx - r, cy - r, r*2, r*2, startAngle, sweep, Arc2D.PIE));
            startAngle += sweep;
        }

        // Inner hole
        g2.setColor(Theme.BG_CARD);
        g2.fillOval(cx - innerR, cy - innerR, innerR*2, innerR*2);

        // Center text
        g2.setColor(Theme.TEXT_PRIMARY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        String tot = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(tot, cx - fm.stringWidth(tot)/2, cy + fm.getAscent()/2 - 4);
        g2.setColor(Theme.TEXT_MUTED);
        g2.setFont(Theme.FONT_SMALL);
        g2.drawString("total", cx - 12, cy + 14);

        // Legend on the right
        int lx = cx + r + 16, ly = cy - (labels.length * 20) / 2;
        for (int i = 0; i < labels.length; i++) {
            g2.setColor(colors[i % colors.length]);
            g2.fillRoundRect(lx, ly + i*22, 10, 10, 4, 4);
            g2.setColor(Theme.TEXT_SECONDARY);
            g2.setFont(Theme.FONT_SMALL);
            g2.drawString(labels[i] + " (" + values[i] + ")", lx + 15, ly + i*22 + 10);
        }
        g2.dispose();
    }
}
