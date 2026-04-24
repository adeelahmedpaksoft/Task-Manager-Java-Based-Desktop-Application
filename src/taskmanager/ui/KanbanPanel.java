package taskmanager.ui;

import taskmanager.command.CommandManager;
import taskmanager.model.Task;
import taskmanager.model.TaskRepository;
import taskmanager.model.User;
import taskmanager.observer.TaskObserver;
import taskmanager.ui.components.Theme;
import taskmanager.ui.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Kanban board view showing tasks in 4 columns.
 * Implements TaskObserver for auto-refresh.
 */
public class KanbanPanel extends JPanel implements TaskObserver {

    private final TaskRepository repo;
    private final CommandManager cmdMgr;
    private final User currentUser;
    private final Runnable onTaskEdit;

    private JPanel todoCol, inProgressCol, reviewCol, doneCol;

    public KanbanPanel(TaskRepository repo, CommandManager cmdMgr,
                       User currentUser, Runnable onTaskEdit) {
        this.repo        = repo;
        this.cmdMgr      = cmdMgr;
        this.currentUser = currentUser;
        this.onTaskEdit  = onTaskEdit;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARK);
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARK);
        header.setBorder(new EmptyBorder(24, 28, 12, 28));
        header.add(UI.label("Kanban Board", Theme.FONT_TITLE, Theme.TEXT_PRIMARY), BorderLayout.WEST);

        JPanel cols = new JPanel(new GridLayout(1, 4, 14, 0));
        cols.setBackground(Theme.BG_DARK);
        cols.setBorder(new EmptyBorder(0, 20, 20, 20));

        todoCol       = makeColumn("TO DO",       Theme.STATUS_TODO);
        inProgressCol = makeColumn("IN PROGRESS", Theme.STATUS_IN_PROGRESS);
        reviewCol     = makeColumn("IN REVIEW",   Theme.STATUS_REVIEW);
        doneCol       = makeColumn("DONE",        Theme.STATUS_DONE);

        cols.add(todoCol); cols.add(inProgressCol); cols.add(reviewCol); cols.add(doneCol);

        add(header, BorderLayout.NORTH);
        add(cols,   BorderLayout.CENTER);
    }

    public void refresh() {
        populate(todoCol,       repo.getByStatus(Task.Status.TODO));
        populate(inProgressCol, repo.getByStatus(Task.Status.IN_PROGRESS));
        populate(reviewCol,     repo.getByStatus(Task.Status.REVIEW));
        populate(doneCol,       repo.getByStatus(Task.Status.DONE));
    }

    @Override public void onTaskChanged(String event, Task task) { refresh(); }

    // ── Column ───────────────────────────────────────────────────────────────

    private JPanel makeColumn(String title, Color accent) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(Theme.BG_SIDEBAR);
        col.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(0, 0, 8, 0)));

        // Column header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(12, 14, 12, 14));
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, 2, 10, 10);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(14, 14));
        dot.setOpaque(false);

        JLabel lbl = UI.label(title, Theme.FONT_LABEL, Theme.TEXT_SECONDARY);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(dot); left.add(lbl);

        // Count badge stored as name so we can update it
        JLabel countBadge = new JLabel("0");
        countBadge.setName("count");
        countBadge.setFont(Theme.FONT_LABEL);
        countBadge.setForeground(Theme.TEXT_MUTED);

        hdr.add(left,       BorderLayout.WEST);
        hdr.add(countBadge, BorderLayout.EAST);

        col.add(hdr);

        // Separator
        JPanel sep = new JPanel();
        sep.setBackground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        col.add(sep);

        col.putClientProperty("accent", accent);
        col.putClientProperty("countBadge", countBadge);
        return col;
    }

    private void populate(JPanel col, List<Task> tasks) {
        // Remove old task cards (keep header and separator = first 2)
        while (col.getComponentCount() > 2) col.remove(2);

        JLabel countBadge = (JLabel) col.getClientProperty("countBadge");
        countBadge.setText(String.valueOf(tasks.size()));

        for (Task t : tasks) {
            col.add(Box.createVerticalStrut(8));
            col.add(makeTaskCard(t));
        }

        // Empty state
        if (tasks.isEmpty()) {
            col.add(Box.createVerticalStrut(20));
            JLabel empty = UI.label("  No tasks", Theme.FONT_SMALL, Theme.TEXT_MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            col.add(empty);
        }

        col.revalidate(); col.repaint();
    }

    // ── Task Card ────────────────────────────────────────────────────────────

    private JPanel makeTaskCard(Task t) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Top row: priority badge + category
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        Color pc = Theme.priorityColor(t.getPriority());
        JLabel priLbl = UI.badge(t.getPriority().toString(), pc);
        JLabel catLbl = UI.label(t.getCategory().toString(), Theme.FONT_SMALL, Theme.TEXT_MUTED);
        top.add(priLbl, BorderLayout.WEST);
        top.add(catLbl, BorderLayout.EAST);

        // Title
        JLabel title = UI.label(t.getTitle(), Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY);
        title.setToolTipText(t.getDescription());

        // Progress bar
        JProgressBar pb = UI.progressBar(t.getProgress(), pc);

        // Bottom: assignee + due date
        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);
        JLabel assignee = UI.label(
                t.getAssignedTo() != null && !t.getAssignedTo().isEmpty()
                        ? "@" + t.getAssignedTo() : "Unassigned",
                Theme.FONT_SMALL, Theme.TEXT_MUTED);
        Color dueColor = t.isOverdue() ? Theme.ACCENT_RED : Theme.TEXT_MUTED;
        JLabel due = UI.label(
                t.getDueDate() != null ? t.getDueDate().toString() : "",
                Theme.FONT_SMALL, dueColor);
        bot.add(assignee, BorderLayout.WEST);
        bot.add(due,      BorderLayout.EAST);

        card.add(top,   BorderLayout.NORTH);
        card.add(title, BorderLayout.CENTER);
        card.add(pb,    BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout(0, 4));
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.CENTER);
        wrap.add(bot,  BorderLayout.SOUTH);

        // Status-advance button
        if (t.getStatus() != Task.Status.DONE && currentUser.isAdmin()) {
            JButton advance = UI.button("→", Theme.BG_ELEVATED);
            advance.setToolTipText("Advance status");
            advance.setPreferredSize(new Dimension(28, 22));
            advance.setFont(Theme.FONT_SUBHEAD);
            Task.Status next = nextStatus(t.getStatus());
            advance.addActionListener(e -> {
                cmdMgr.execute(CommandManager.updateStatus(repo, t.getId(), next));
            });
            top.add(advance, BorderLayout.CENTER);
        }

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(Theme.BG_ELEVATED); card.repaint();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Theme.BG_CARD); card.repaint();
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) onTaskEdit.run();
            }
        });
        return wrap;
    }

    private Task.Status nextStatus(Task.Status s) {
        return switch (s) {
            case TODO        -> Task.Status.IN_PROGRESS;
            case IN_PROGRESS -> Task.Status.REVIEW;
            case REVIEW      -> Task.Status.DONE;
            default          -> Task.Status.DONE;
        };
    }
}
