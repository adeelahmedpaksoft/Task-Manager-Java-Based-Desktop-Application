package taskmanager.ui.login;

import taskmanager.auth.AuthService;
import taskmanager.model.User;
import taskmanager.ui.components.Theme;
import taskmanager.ui.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.function.Consumer;

/**
 * Full-screen animated login panel.
 * Supports Admin and Developer roles.
 */
public class LoginPanel extends JPanel {

    private final Consumer<User> onLoginSuccess;

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;
    private JLabel         titleLabel;
    private float          animAlpha = 0f;

    public LoginPanel(Consumer<User> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setLayout(new GridBagLayout());
        setBackground(Theme.BG_DARKEST);
        buildUI();
        startFadeIn();
    }

    // ── UI ───────────────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Glow background
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(20, 25, 45),
                        getWidth(), getHeight(), new Color(12, 15, 30));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                // Border glow
                g2.setColor(new Color(99, 179, 237, 60));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 20, 20));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 540));
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 0, 8, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.weightx = 1;

        // Logo / icon
        gc.gridy = 0; gc.insets = new Insets(0, 0, 5, 0);
        JLabel logoLabel = new JLabel("⬡", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        logoLabel.setForeground(Theme.ACCENT_BLUE);
        card.add(logoLabel, gc);

        // Title
        gc.gridy = 1; gc.insets = new Insets(0, 0, 4, 0);
        titleLabel = new JLabel("TaskManager Pro", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        card.add(titleLabel, gc);

        // Subtitle
        gc.gridy = 2; gc.insets = new Insets(0, 0, 30, 0);
        JLabel subLabel = new JLabel("Sign in to your workspace", SwingConstants.CENTER);
        subLabel.setFont(Theme.FONT_BODY);
        subLabel.setForeground(Theme.TEXT_MUTED);
        card.add(subLabel, gc);

        // Quick login buttons
        gc.gridy = 3; gc.insets = new Insets(0, 0, 20, 0);
        JPanel quickPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        quickPanel.setOpaque(false);
        JButton adminQuick = quickLoginBtn("Admin", "admin", "admin123", new Color(99, 179, 237));
        JButton devQuick   = quickLoginBtn("Developer", "dev1", "dev123", new Color(159, 122, 234));
        adminQuick.addActionListener(e -> { usernameField.setText("admin"); passwordField.setText("admin123"); doLogin(); });
        devQuick.addActionListener(e  -> { usernameField.setText("dev1");  passwordField.setText("dev123");   doLogin(); });
        quickPanel.add(adminQuick); quickPanel.add(devQuick);
        card.add(quickPanel, gc);

        // Divider
        gc.gridy = 4; gc.insets = new Insets(0, 0, 20, 0);
        JPanel divider = new JPanel(new BorderLayout(10, 0));
        divider.setOpaque(false);
        JSeparator s1 = UI.sep(), s2 = UI.sep();
        JLabel orLabel = new JLabel("or sign in manually", SwingConstants.CENTER);
        orLabel.setFont(Theme.FONT_SMALL);
        orLabel.setForeground(Theme.TEXT_MUTED);
        divider.add(s1, BorderLayout.WEST); divider.add(orLabel, BorderLayout.CENTER); divider.add(s2, BorderLayout.EAST);
        s1.setPreferredSize(new Dimension(100, 1)); s2.setPreferredSize(new Dimension(100, 1));
        card.add(divider, gc);

        // Username
        gc.gridy = 5; gc.insets = new Insets(0, 0, 6, 0);
        card.add(fieldLabel("Username"), gc);
        gc.gridy = 6; gc.insets = new Insets(0, 0, 14, 0);
        usernameField = UI.textField("");
        usernameField.setPreferredSize(new Dimension(340, 44));
        card.add(usernameField, gc);

        // Password
        gc.gridy = 7; gc.insets = new Insets(0, 0, 6, 0);
        card.add(fieldLabel("Password"), gc);
        gc.gridy = 8; gc.insets = new Insets(0, 0, 6, 0);
        passwordField = UI.passwordField();
        passwordField.setPreferredSize(new Dimension(340, 44));
        card.add(passwordField, gc);

        // Error label
        gc.gridy = 9; gc.insets = new Insets(0, 0, 16, 0);
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.ACCENT_RED);
        card.add(errorLabel, gc);

        // Login button
        gc.gridy = 10; gc.insets = new Insets(0, 0, 20, 0);
        JButton loginBtn = UI.button("Sign In", Theme.ACCENT_BLUE);
        loginBtn.setPreferredSize(new Dimension(340, 44));
        loginBtn.setFont(Theme.FONT_HEADING);
        card.add(loginBtn, gc);

        // Hint
        gc.gridy = 11; gc.insets = new Insets(0, 0, 0, 0);
        JLabel hint = new JLabel("Default: admin/admin123 or dev1/dev123", SwingConstants.CENTER);
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        card.add(hint, gc);

        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        add(card);
    }

    // ── Background painting ──────────────────────────────────────────────────

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        // Subtle grid pattern
        g2.setColor(new Color(255, 255, 255, 8));
        for (int x = 0; x < getWidth(); x += 40) g2.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += 40) g2.drawLine(0, y, getWidth(), y);
        // Radial glow
        RadialGradientPaint rgp = new RadialGradientPaint(
                new Point(getWidth()/2, getHeight()/2),
                Math.max(getWidth(), getHeight()) * 0.6f,
                new float[]{0f, 1f},
                new Color[]{new Color(30, 40, 80, 80), new Color(0, 0, 0, 0)});
        g2.setPaint(rgp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        errorLabel.setText(" ");

        if (user.isEmpty() || pass.isEmpty()) {
            shake(); errorLabel.setText("Please enter username and password.");
            return;
        }

        User u = AuthService.getInstance().login(user, pass);
        if (u != null) {
            onLoginSuccess.accept(u);
        } else {
            shake();
            errorLabel.setText("Invalid username or password.");
            passwordField.setText("");
        }
    }

    private void shake() {
        final int[] count = {0};
        final Point orig = getLocationOnScreen();
        Timer t = new Timer(30, null);
        t.addActionListener(e -> {
            count[0]++;
            int ox = (count[0] % 2 == 0) ? 8 : -8;
            JPanel card = (JPanel) getComponent(0);
            card.setLocation(card.getX() + ox, card.getY());
            if (count[0] > 8) { t.stop(); card.setLocation(card.getX(), card.getY()); }
        });
        t.start();
    }

    private void startFadeIn() {
        Timer t = new Timer(16, null);
        t.addActionListener(e -> {
            animAlpha = Math.min(1f, animAlpha + 0.04f);
            setBackground(new Color(
                    (int)(Theme.BG_DARKEST.getRed()   * animAlpha),
                    (int)(Theme.BG_DARKEST.getGreen() * animAlpha),
                    (int)(Theme.BG_DARKEST.getBlue()  * animAlpha)));
            repaint();
            if (animAlpha >= 1f) t.stop();
        });
        t.start();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JButton quickLoginBtn(String label, String user, String pass, Color color) {
        JButton b = UI.button(label, Theme.BG_ELEVATED);
        b.setFont(Theme.FONT_SUBHEAD);
        b.setForeground(color);
        b.setPreferredSize(new Dimension(0, 42));
        return b;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }
}
