package taskmanager.ui;

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
 * Left sidebar with navigation links and user profile.
 */
public class SidebarPanel extends JPanel {

    public enum Page { DASHBOARD, TASKS, KANBAN, TODO, SETTINGS }

    private final Consumer<Page> onNavigate;
    private final User           currentUser;
    private final Runnable       onLogout;

    private Page activePage = Page.DASHBOARD;
    private final JPanel navContainer = new JPanel();

    record NavItem(String icon, String label, Page page) {}

    private static final NavItem[] NAV_ITEMS = {
        new NavItem("⊞",  "Dashboard", Page.DASHBOARD),
        new NavItem("☰",  "All Tasks",  Page.TASKS),
        new NavItem("⬜", "Kanban",     Page.KANBAN),
        new NavItem("✓",  "To-Do",      Page.TODO),
    };

    public SidebarPanel(User currentUser, Consumer<Page> onNavigate, Runnable onLogout) {
        this.currentUser  = currentUser;
        this.onNavigate   = onNavigate;
        this.onLogout     = onLogout;
        setPreferredSize(new Dimension(220, 0));
        setBackground(Theme.BG_SIDEBAR);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        // ── Logo ──────────────────────────────────────────────────────────
        JPanel logo = new JPanel(new BorderLayout());
        logo.setOpaque(false);
        logo.setBorder(new EmptyBorder(22, 18, 22, 18));
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel logoIcon = new JLabel("⬡");
        logoIcon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        logoIcon.setForeground(Theme.ACCENT_BLUE);

        JLabel logoText = UI.label("TaskManager", Theme.FONT_HEADING, Theme.TEXT_PRIMARY);
        JLabel ver      = UI.label("Pro v2.0", Theme.FONT_SMALL, Theme.TEXT_MUTED);

        JPanel logoText2 = new JPanel(); logoText2.setLayout(new BoxLayout(logoText2, BoxLayout.Y_AXIS));
        logoText2.setOpaque(false);
        logoText2.add(logoText); logoText2.add(ver);

        logo.add(logoIcon,  BorderLayout.WEST);
        logo.add(Box.createHorizontalStrut(10), BorderLayout.CENTER);
        logo.add(logoText2, BorderLayout.EAST);
        top.add(logo);

        // ── Role badge ────────────────────────────────────────────────────
        JPanel roleBadgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        roleBadgeWrap.setOpaque(false);
        roleBadgeWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        Color roleColor = currentUser.isAdmin() ? Theme.ACCENT_ORANGE : Theme.ACCENT_PURPLE;
        roleBadgeWrap.add(UI.badge(currentUser.getRole().toString(), roleColor));
        top.add(roleBadgeWrap);
        top.add(Box.createVerticalStrut(12));

        // ── Nav items ─────────────────────────────────────────────────────
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setOpaque(false);
        for (NavItem item : NAV_ITEMS) {
            navContainer.add(makeNavBtn(item));
        }

        // Settings (only for admin)
        if (currentUser.isAdmin()) {
            navContainer.add(makeNavBtn(new NavItem("⚙", "Settings", Page.SETTINGS)));
        }

        top.add(navContainer);
        add(top, BorderLayout.NORTH);

        // ── User profile (bottom) ─────────────────────────────────────────
        JPanel profile = makeProfilePanel();
        add(profile, BorderLayout.SOUTH);
    }

    private JPanel makeNavBtn(NavItem item) {
        JPanel btn = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = activePage == item.page();
                if (active) {
                    g2.setColor(Theme.withAlpha(Theme.ACCENT_BLUE, 25));
                    g2.fill(new RoundRectangle2D.Float(8, 2, getWidth()-16, getHeight()-4, 8, 8));
                    // Left accent bar
                    g2.setColor(Theme.ACCENT_BLUE);
                    g2.fill(new RoundRectangle2D.Float(0, 4, 4, getHeight()-8, 4, 4));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(11, 18, 11, 18));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        boolean active = activePage == item.page();
        Color iconColor = active ? Theme.ACCENT_BLUE : Theme.TEXT_MUTED;
        Color textColor = active ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY;

        JLabel iconLbl = UI.label(item.icon(), new Font("Segoe UI", Font.PLAIN, 16), iconColor);
        iconLbl.setPreferredSize(new Dimension(22, 22));
        JLabel textLbl = UI.label(item.label(), Theme.FONT_SUBHEAD, textColor);

        btn.add(iconLbl, BorderLayout.WEST);
        btn.add(textLbl, BorderLayout.CENTER);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                activePage = item.page();
                refreshNav();
                onNavigate.accept(item.page());
            }
            @Override public void mouseEntered(MouseEvent e) {
                if (activePage != item.page()) btn.setBackground(Theme.BG_HOVER);
                btn.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(null); btn.repaint();
            }
        });
        return btn;
    }

    private void refreshNav() {
        navContainer.removeAll();
        for (NavItem item : NAV_ITEMS) navContainer.add(makeNavBtn(item));
        if (currentUser.isAdmin()) navContainer.add(makeNavBtn(new NavItem("⚙","Settings",Page.SETTINGS)));
        navContainer.revalidate(); navContainer.repaint();
    }

    public void setActivePage(Page page) {
        activePage = page;
        refreshNav();
    }

    private JPanel makeProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Theme.BG_SIDEBAR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                new EmptyBorder(14, 16, 14, 16)));

        // Avatar circle
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = currentUser.isAdmin() ? Theme.ACCENT_ORANGE : Theme.ACCENT_PURPLE;
                g2.setColor(Theme.withAlpha(c, 40));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(c);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String initials = currentUser.getAvatarInitials();
                g2.drawString(initials,
                        (getWidth()  - fm.stringWidth(initials)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setOpaque(false);

        JLabel name = UI.label(currentUser.getDisplayName(), Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY);
        JLabel role = UI.label(currentUser.getRole().toString(), Theme.FONT_SMALL, Theme.TEXT_MUTED);
        JPanel info = new JPanel(); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.add(name); info.add(role);

        JButton logoutBtn = new JButton("⏻") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(getModel().isRollover() ? Theme.ACCENT_RED : Theme.TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                g2.drawString("⏻", 4, 18);
                g2.dispose();
            }
        };
        logoutBtn.setPreferredSize(new Dimension(28, 28));
        logoutBtn.setOpaque(false); logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false); logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setToolTipText("Logout");
        logoutBtn.addActionListener(e -> onLogout.run());

        panel.add(avatar,    BorderLayout.WEST);
        panel.add(info,      BorderLayout.CENTER);
        panel.add(logoutBtn, BorderLayout.EAST);
        return panel;
    }
}
