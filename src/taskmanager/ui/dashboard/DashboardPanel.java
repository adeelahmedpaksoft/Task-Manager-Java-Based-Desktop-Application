package taskmanager.ui.dashboard;

import taskmanager.model.Task;
import taskmanager.model.TaskRepository;
import taskmanager.model.User;
import taskmanager.observer.TaskObserver;
import taskmanager.ui.components.Theme;
import taskmanager.ui.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Interactive dashboard showing stats, charts and recent activity.
 * Implements TaskObserver so it refreshes automatically.
 */
public class DashboardPanel extends JPanel implements TaskObserver {

    private final TaskRepository repo;
    private final User currentUser;

    private JPanel statsPanel;
    private MiniBarChart barChart;
    private DonutChart donutChart;
    private JPanel recentPanel;
    private JLabel greetingLabel;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd");

    public DashboardPanel(TaskRepository repo, User currentUser) {
        this.repo        = repo;
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_DARK);
        buildUI();
        refresh();
    }

    // ── Build ────────────────────────────────────────────────────────────────

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARK);
        header.setBorder(new EmptyBorder(28, 28, 14, 28));

        greetingLabel = UI.label("", Theme.FONT_TITLE, Theme.TEXT_PRIMARY);
        JLabel subHead = UI.label("Here's what's happening with your projects today.",
                Theme.FONT_BODY, Theme.TEXT_MUTED);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        textCol.add(greetingLabel);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(subHead);
        header.add(textCol, BorderLayout.WEST);

        JLabel dateLabel = UI.label(
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy")),
                Theme.FONT_BODY, Theme.TEXT_MUTED);
        header.add(dateLabel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Main content scroll
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG_DARK);
        content.setBorder(new EmptyBorder(0, 20, 20, 20));

        // Stats row
        statsPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        content.add(statsPanel);
        content.add(Box.createVerticalStrut(18));

        // Charts row
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 14, 0));
        chartsRow.setOpaque(false);
        chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        barChart   = new MiniBarChart();
        donutChart = new DonutChart();

        JPanel barCard = wrapInCard("Tasks by Category", barChart);
        JPanel donCard = wrapInCard("Status Overview",   donutChart);
        chartsRow.add(barCard);
        chartsRow.add(donCard);
        content.add(chartsRow);
        content.add(Box.createVerticalStrut(18));

        // Recent tasks
        recentPanel = new JPanel();
        recentPanel.setLayout(new BoxLayout(recentPanel, BoxLayout.Y_AXIS));
        recentPanel.setOpaque(false);
        recentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        JPanel recentCard = wrapInCard("Recent Activity", recentPanel);
        content.add(recentCard);

        add(UI.scroll(content), BorderLayout.CENTER);
    }

    // ── Refresh ──────────────────────────────────────────────────────────────

    public void refresh() {
        greetingLabel.setText("Welcome back, " + currentUser.getDisplayName() + " 👋");

        List<Task> all = repo.getAllTasks();

        // Rebuild stats
        statsPanel.removeAll();
        statsPanel.add(statCard("Total Tasks",   String.valueOf(all.size()),          Theme.ACCENT_BLUE,   "⊞"));
        statsPanel.add(statCard("In Progress",   String.valueOf(repo.countByStatus(Task.Status.IN_PROGRESS)), Theme.ACCENT_CYAN, "◑"));
        statsPanel.add(statCard("Completed",     String.valueOf(repo.countByStatus(Task.Status.DONE)),        Theme.ACCENT_GREEN, "✔"));
        statsPanel.add(statCard("Overdue",       String.valueOf(repo.countOverdue()),  Theme.ACCENT_RED,    "⚠"));
        statsPanel.revalidate(); statsPanel.repaint();

        // Charts
        barChart.setData(
                new String[]{"Feature","Bug","Improve","Docs","Testing","Other"},
                new int[]{
                        (int) all.stream().filter(t -> t.getCategory() == Task.Category.FEATURE).count(),
                        (int) all.stream().filter(t -> t.getCategory() == Task.Category.BUG).count(),
                        (int) all.stream().filter(t -> t.getCategory() == Task.Category.IMPROVEMENT).count(),
                        (int) all.stream().filter(t -> t.getCategory() == Task.Category.DOCUMENTATION).count(),
                        (int) all.stream().filter(t -> t.getCategory() == Task.Category.TESTING).count(),
                        (int) all.stream().filter(t -> t.getCategory() == Task.Category.OTHER).count()
                },
                new Color[]{Theme.ACCENT_BLUE, Theme.ACCENT_RED, Theme.ACCENT_GREEN,
                        Theme.ACCENT_ORANGE, Theme.ACCENT_PURPLE, Theme.ACCENT_CYAN});

        donutChart.setData(
                new String[]{"To Do","In Progress","Review","Done"},
                new int[]{
                        (int) repo.countByStatus(Task.Status.TODO),
                        (int) repo.countByStatus(Task.Status.IN_PROGRESS),
                        (int) repo.countByStatus(Task.Status.REVIEW),
                        (int) repo.countByStatus(Task.Status.DONE)
                },
                new Color[]{Theme.STATUS_TODO, Theme.STATUS_IN_PROGRESS, Theme.STATUS_REVIEW, Theme.STATUS_DONE});

        // Recent tasks list (last 6)
        recentPanel.removeAll();
        List<Task> recent = all.subList(Math.max(0, all.size() - 6), all.size());
        for (int i = recent.size()-1; i >= 0; i--) {
            recentPanel.add(recentRow(recent.get(i)));
            if (i > 0) recentPanel.add(Box.createVerticalStrut(6));
        }
        if (all.isEmpty()) {
            JLabel empty = UI.label("No tasks yet. Add your first task!", Theme.FONT_BODY, Theme.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            recentPanel.add(empty);
        }
        recentPanel.revalidate(); recentPanel.repaint();
    }

    // ── Observer ─────────────────────────────────────────────────────────────

    @Override public void onTaskChanged(String event, Task task) { refresh(); }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JPanel statCard(String title, String value, Color accent, String icon) {
        JPanel card = UI.card(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setPreferredSize(new Dimension(0, 110));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel t = UI.label(title, Theme.FONT_SMALL, Theme.TEXT_MUTED);
        JLabel ic = UI.label(icon, new Font("Segoe UI", Font.PLAIN, 20), accent);
        top.add(t, BorderLayout.WEST); top.add(ic, BorderLayout.EAST);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 36));
        val.setForeground(accent);

        // Animated count-up
        try {
            int target = Integer.parseInt(value);
            Timer anim = new Timer(16, null);
            final int[] cur = {0};
            anim.addActionListener(e -> {
                cur[0] = Math.min(target, cur[0] + Math.max(1, target / 20));
                val.setText(String.valueOf(cur[0]));
                if (cur[0] >= target) ((Timer)e.getSource()).stop();
            });
            anim.start();
        } catch (NumberFormatException ignored) {}

        // Bottom accent bar
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.withAlpha(accent, 60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 3));

        card.add(top, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        card.add(bar, BorderLayout.SOUTH);
        return card;
    }

    private JPanel recentRow(Task t) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 12, 8, 12));
        row.setBackground(Theme.BG_ELEVATED);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        // Colour dot
        Color c = Theme.priorityColor(t.getPriority());
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillOval(0, 0, 10, 10);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(10, 10));
        dot.setOpaque(false);

        JPanel dotWrap = new JPanel(new GridBagLayout());
        dotWrap.setOpaque(false);
        dotWrap.add(dot);

        JLabel title = UI.label(t.getTitle(), Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY);
        JLabel cat   = UI.label(t.getCategory().toString(), Theme.FONT_SMALL, Theme.TEXT_MUTED);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        textCol.add(title); textCol.add(cat);

        JLabel statusLbl = UI.badge(t.getStatus().toString().replace("_"," "), Theme.statusColor(t.getStatus()));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(statusLbl);
        if (t.getDueDate() != null)
            right.add(UI.label(t.getDueDate().format(DATE_FMT), Theme.FONT_SMALL, Theme.TEXT_MUTED));

        row.add(dotWrap, BorderLayout.WEST);
        row.add(textCol, BorderLayout.CENTER);
        row.add(right,   BorderLayout.EAST);

        // hover effect
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                row.setBackground(Theme.BG_HOVER); row.setOpaque(true); row.repaint();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                row.setBackground(Theme.BG_ELEVATED); row.setOpaque(false); row.repaint();
            }
        });
        return row;
    }

    private JPanel wrapInCard(String title, JComponent content) {
        JPanel card = UI.card(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel lbl = UI.label(title, Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY);
        card.add(lbl,     BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }
}
