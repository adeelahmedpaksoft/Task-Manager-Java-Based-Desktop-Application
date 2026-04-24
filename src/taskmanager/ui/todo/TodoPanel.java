package taskmanager.ui.todo;

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
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.List;

/**
 * Interactive To-Do list panel with quick-add, checkboxes, and filtering.
 * Implements TaskObserver for automatic refresh.
 */
public class TodoPanel extends JPanel implements TaskObserver {

    private final TaskRepository repo;
    private final CommandManager cmdMgr;
    private final User currentUser;

    private JPanel    listPanel;
    private JTextField quickAddField;
    private JComboBox<String>          filterBox;
    private JLabel    statsLabel;

    public TodoPanel(TaskRepository repo, CommandManager cmdMgr, User currentUser) {
        this.repo        = repo;
        this.cmdMgr      = cmdMgr;
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARK);
        buildUI();
        refresh();
    }

    // ── Build ────────────────────────────────────────────────────────────────

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARK);
        header.setBorder(new EmptyBorder(24, 28, 16, 28));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(UI.label("My To-Do List", Theme.FONT_TITLE, Theme.TEXT_PRIMARY), BorderLayout.WEST);

        statsLabel = UI.label("", Theme.FONT_BODY, Theme.ACCENT_GREEN);
        statsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        titleRow.add(statsLabel, BorderLayout.EAST);

        header.add(titleRow, BorderLayout.NORTH);
        header.add(Box.createVerticalStrut(14), BorderLayout.CENTER);

        // Quick-add row
        JPanel addRow = new JPanel(new BorderLayout(10, 0));
        addRow.setOpaque(false);

        quickAddField = UI.textField("  + Add a new task... (press Enter)");
        quickAddField.setPreferredSize(new Dimension(0, 44));
        quickAddField.addActionListener(e -> quickAdd());

        JButton addBtn = UI.button("Add", Theme.ACCENT_BLUE);
        addBtn.setPreferredSize(new Dimension(80, 44));
        addBtn.addActionListener(e -> quickAdd());

        addRow.add(quickAddField, BorderLayout.CENTER);
        addRow.add(addBtn,        BorderLayout.EAST);

        // Filter row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        filterRow.setOpaque(false);

        filterRow.add(UI.label("Show: ", Theme.FONT_SMALL, Theme.TEXT_MUTED));
        String[] filters = {"All", "To Do", "In Progress", "Review", "Done", "My Tasks", "Overdue"};
        filterBox = UI.combo(filters);
        filterBox.setPreferredSize(new Dimension(140, 32));
        filterBox.addActionListener(e -> refresh());
        filterRow.add(filterBox);

        JPanel headerSouth = new JPanel();
        headerSouth.setLayout(new BoxLayout(headerSouth, BoxLayout.Y_AXIS));
        headerSouth.setOpaque(false);
        headerSouth.add(addRow);
        headerSouth.add(filterRow);

        header.add(headerSouth, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // List
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Theme.BG_DARK);
        listPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        add(UI.scroll(listPanel), BorderLayout.CENTER);
    }

    // ── Refresh ──────────────────────────────────────────────────────────────

    public void refresh() {
        List<Task> all = repo.getAllTasks();
        String filter  = (String) filterBox.getSelectedItem();

        List<Task> shown = all.stream().filter(t -> switch (filter) {
            case "To Do"       -> t.getStatus() == Task.Status.TODO;
            case "In Progress" -> t.getStatus() == Task.Status.IN_PROGRESS;
            case "Review"      -> t.getStatus() == Task.Status.REVIEW;
            case "Done"        -> t.getStatus() == Task.Status.DONE;
            case "My Tasks"    -> currentUser.getUsername().equalsIgnoreCase(t.getAssignedTo());
            case "Overdue"     -> t.isOverdue();
            default            -> true;
        }).toList();

        long done  = all.stream().filter(t -> t.getStatus() == Task.Status.DONE).count();
        statsLabel.setText(done + " / " + all.size() + " completed");

        listPanel.removeAll();

        if (shown.isEmpty()) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setOpaque(false);
            empty.setBorder(new EmptyBorder(60, 0, 0, 0));
            JLabel e1 = UI.label("✓ All clear!", new Font("Segoe UI", Font.BOLD, 32), Theme.ACCENT_GREEN);
            e1.setHorizontalAlignment(SwingConstants.CENTER);
            JLabel e2 = UI.label("No tasks matching this filter.", Theme.FONT_BODY, Theme.TEXT_MUTED);
            e2.setHorizontalAlignment(SwingConstants.CENTER);
            empty.add(e1, BorderLayout.CENTER);
            empty.add(e2, BorderLayout.SOUTH);
            listPanel.add(empty);
        } else {
            for (Task t : shown) {
                listPanel.add(makeTodoRow(t));
                listPanel.add(Box.createVerticalStrut(6));
            }
        }
        listPanel.revalidate(); listPanel.repaint();
    }

    @Override public void onTaskChanged(String event, Task task) { refresh(); }

    // ── Quick Add ────────────────────────────────────────────────────────────

    private void quickAdd() {
        String text = quickAddField.getText().trim();
        if (text.isEmpty() || text.startsWith("+")) return;
        cmdMgr.execute(CommandManager.addTask(
                repo, text, "", Task.Priority.MEDIUM, Task.Category.OTHER,
                LocalDate.now().plusDays(3), currentUser.getUsername()));
        quickAddField.setText("");
        quickAddField.requestFocus();
    }

    // ── Row ──────────────────────────────────────────────────────────────────

    private JPanel makeTodoRow(Task t) {
        JPanel row = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                // Left accent stripe
                Color stripe = Theme.priorityColor(t.getPriority());
                g2.setColor(stripe);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(10, 16, 10, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        // Checkbox (Done toggle)
        boolean isDone = t.getStatus() == Task.Status.DONE;
        JCheckBox check = new JCheckBox();
        check.setSelected(isDone);
        check.setOpaque(false);
        check.setFocusPainted(false);
        check.addActionListener(e -> {
            Task.Status ns = check.isSelected() ? Task.Status.DONE : Task.Status.TODO;
            cmdMgr.execute(CommandManager.updateStatus(repo, t.getId(), ns));
        });

        // Title + meta
        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel titleLbl = UI.label(t.getTitle(), Theme.FONT_SUBHEAD,
                isDone ? Theme.TEXT_MUTED : Theme.TEXT_PRIMARY);
        if (isDone) {
            // Strike-through
            titleLbl.setText("<html><strike>" + t.getTitle() + "</strike></html>");
        }

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        metaRow.setOpaque(false);
        metaRow.add(UI.badge(t.getPriority().toString(), Theme.priorityColor(t.getPriority())));
        metaRow.add(UI.badge(t.getCategory().toString(), Theme.TEXT_MUTED));
        if (t.getAssignedTo() != null && !t.getAssignedTo().isEmpty())
            metaRow.add(UI.label("@" + t.getAssignedTo(), Theme.FONT_SMALL, Theme.TEXT_MUTED));

        textCol.add(titleLbl);
        textCol.add(metaRow);

        // Right panel: progress + due date + delete
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        // Progress slider
        JSlider slider = new JSlider(0, 100, t.getProgress());
        slider.setPreferredSize(new Dimension(90, 20));
        slider.setBackground(Theme.BG_CARD);
        slider.setForeground(Theme.ACCENT_BLUE);
        slider.setOpaque(false);
        slider.addChangeListener(e -> {
            if (!slider.getValueIsAdjusting()) {
                cmdMgr.execute(CommandManager.updateProgress(repo, t.getId(), slider.getValue()));
            }
        });

        JLabel pct = UI.label(t.getProgress() + "%", Theme.FONT_SMALL, Theme.ACCENT_BLUE);
        slider.addChangeListener(e -> pct.setText(slider.getValue() + "%"));

        if (t.getDueDate() != null) {
            Color dc = t.isOverdue() ? Theme.ACCENT_RED : Theme.TEXT_MUTED;
            right.add(UI.label(t.getDueDate().toString(), Theme.FONT_SMALL, dc));
        }
        right.add(pct);
        right.add(slider);

        // Delete button (admin only)
        if (currentUser.isAdmin()) {
            JButton del = UI.button("✕", Theme.BG_ELEVATED);
            del.setForeground(Theme.ACCENT_RED);
            del.setPreferredSize(new Dimension(28, 28));
            del.setFont(new Font("Segoe UI", Font.BOLD, 11));
            del.setToolTipText("Remove task");
            del.addActionListener(e -> cmdMgr.execute(CommandManager.removeTask(repo, t.getId())));
            right.add(del);
        }

        row.add(check,   BorderLayout.WEST);
        row.add(textCol, BorderLayout.CENTER);
        row.add(right,   BorderLayout.EAST);

        // Hover
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { row.setBackground(Theme.BG_HOVER); row.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { row.setBackground(Theme.BG_CARD);  row.repaint(); }
        });
        return row;
    }
}
