package taskmanager.ui.components;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Factory for styled reusable UI components.
 */
public final class UI {

    private UI() {}

    // ── Styled button ────────────────────────────────────────────────────────

    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed()   ? bg.darker() :
                          getModel().isRollover()  ? bg.brighter() : bg;
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        b.setFont(Theme.FONT_SUBHEAD);
        b.setForeground(Theme.TEXT_PRIMARY);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 20, 36));
        return b;
    }

    public static JButton iconButton(String text, Color bg, int w, int h) {
        JButton b = button(text, bg);
        b.setPreferredSize(new Dimension(w, h));
        return b;
    }

    // ── Card panel ───────────────────────────────────────────────────────────

    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 14, 14));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    // ── Styled text field ────────────────────────────────────────────────────

    public static JTextField textField(String placeholder) {
        JTextField tf = new JTextField(placeholder) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_ELEVATED);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        tf.setFont(Theme.FONT_BODY);
        tf.setForeground(Theme.TEXT_PRIMARY);
        tf.setBackground(Theme.BG_ELEVATED);
        tf.setCaretColor(Theme.ACCENT_BLUE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        tf.setOpaque(false);
        return tf;
    }

    // ── Password field ───────────────────────────────────────────────────────

    public static JPasswordField passwordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(Theme.FONT_BODY);
        pf.setForeground(Theme.TEXT_PRIMARY);
        pf.setBackground(Theme.BG_ELEVATED);
        pf.setCaretColor(Theme.ACCENT_BLUE);
        pf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        pf.setEchoChar('●');
        return pf;
    }

    // ── Label helpers ────────────────────────────────────────────────────────

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JLabel heading(String text) {
        return label(text, Theme.FONT_HEADING, Theme.TEXT_PRIMARY);
    }

    public static JLabel subLabel(String text) {
        return label(text, Theme.FONT_SMALL, Theme.TEXT_MUTED);
    }

    // ── Separator ────────────────────────────────────────────────────────────

    public static JSeparator sep() {
        JSeparator s = new JSeparator();
        s.setForeground(Theme.BORDER);
        s.setBackground(Theme.BORDER);
        return s;
    }

    // ── Badge pill ───────────────────────────────────────────────────────────

    public static JLabel badge(String text, Color bg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.withAlpha(bg, 40));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.setColor(bg);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, getHeight(), getHeight()));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(bg);
        l.setOpaque(false);
        l.setBorder(new EmptyBorder(2, 10, 2, 10));
        return l;
    }

    // ── Scroll pane ──────────────────────────────────────────────────────────

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setBackground(Theme.BG_DARK);
        sp.getViewport().setBackground(Theme.BG_DARK);
        sp.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new DarkScrollBarUI());
        return sp;
    }

    // ── ComboBox ─────────────────────────────────────────────────────────────

    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(Theme.FONT_BODY);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setBackground(Theme.BG_ELEVATED);
        cb.setBorder(new LineBorder(Theme.BORDER, 1, true));
        return cb;
    }

    // ── Progress bar ─────────────────────────────────────────────────────────

    public static JProgressBar progressBar(int value, Color color) {
        JProgressBar pb = new JProgressBar(0, 100) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_ELEVATED);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                if (getValue() > 0) {
                    float w = getWidth() * getValue() / 100f;
                    g2.setColor(color);
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, getHeight(), getHeight(), getHeight()));
                }
                g2.dispose();
            }
        };
        pb.setValue(value);
        pb.setOpaque(false);
        pb.setBorderPainted(false);
        pb.setStringPainted(false);
        pb.setPreferredSize(new Dimension(100, 8));
        return pb;
    }
}
