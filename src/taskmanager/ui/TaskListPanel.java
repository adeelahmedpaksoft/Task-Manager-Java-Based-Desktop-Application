package taskmanager.ui;

import taskmanager.command.CommandManager;
import taskmanager.model.Task;
import taskmanager.model.TaskRepository;
import taskmanager.model.User;
import taskmanager.observer.TaskObserver;
import taskmanager.strategy.FilterStrategies;
import taskmanager.strategy.FilterStrategy;
import taskmanager.ui.components.Theme;
import taskmanager.ui.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Full task list panel with table, search, and filter controls.
 */
public class TaskListPanel extends JPanel implements TaskObserver {

    private final TaskRepository repo;
    private final CommandManager cmdMgr;
    private final User           currentUser;
    private final Runnable       onAddTask;
    private final java.util.function.Consumer<Task> onEditTask;

    private final TaskTableModel tableModel = new TaskTableModel();
    private final JTable         table      = new JTable(tableModel);
    private JTextField           searchField;
    private JComboBox<FilterStrategy> filterBox;
    private FilterStrategy[]     strategies;
    private JLabel               countLabel;

    public TaskListPanel(TaskRepository repo, CommandManager cmdMgr,
                         User currentUser, Runnable onAddTask,
                         java.util.function.Consumer<Task> onEditTask) {
        this.repo        = repo;
        this.cmdMgr      = cmdMgr;
        this.currentUser = currentUser;
        this.onAddTask   = onAddTask;
        this.onEditTask  = onEditTask;
        strategies       = FilterStrategies.all(currentUser.getUsername());
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARK);
        buildUI();
        refresh();
    }

    private void buildUI() {
        // ── Header ────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(Theme.BG_DARK);
        header.setBorder(new EmptyBorder(24, 28, 14, 28));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(UI.label("All Tasks", Theme.FONT_TITLE, Theme.TEXT_PRIMARY), BorderLayout.WEST);

        JButton addBtn = UI.button("＋  New Task", Theme.ACCENT_BLUE);
        addBtn.setPreferredSize(new Dimension(130, 38));
        addBtn.addActionListener(e -> onAddTask.run());
        titleRow.add(addBtn, BorderLayout.EAST);
        header.add(titleRow, BorderLayout.NORTH);

        // Search + filter bar
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(12, 0, 0, 0));

        searchField = UI.textField("  🔍  Search tasks...");
        searchField.setPreferredSize(new Dimension(0, 38));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
        });

        filterBox = new JComboBox<>(strategies);
        filterBox.setRenderer((list, val, idx, sel, focus) -> {
            JLabel l = new JLabel(val != null ? val.getIcon() + "  " + val.getName() : "");
            l.setFont(Theme.FONT_BODY);
            l.setForeground(sel ? Theme.TEXT_INVERSE : Theme.TEXT_PRIMARY);
            l.setBackground(sel ? Theme.ACCENT_BLUE : Theme.BG_ELEVATED);
            l.setOpaque(true);
            l.setBorder(new EmptyBorder(4, 10, 4, 10));
            return l;
        });
        filterBox.setBackground(Theme.BG_ELEVATED);
        filterBox.setForeground(Theme.TEXT_PRIMARY);
        filterBox.setFont(Theme.FONT_BODY);
        filterBox.setPreferredSize(new Dimension(160, 38));
        filterBox.addActionListener(e -> refresh());

        countLabel = UI.label("", Theme.FONT_SMALL, Theme.TEXT_MUTED);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(countLabel);
        right.add(filterBox);

        bar.add(searchField, BorderLayout.CENTER);
        bar.add(right,       BorderLayout.EAST);
        header.add(bar, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────
        styleTable();
        JScrollPane scroll = UI.scroll(table);
        scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
        add(scroll, BorderLayout.CENTER);

        // ── Bottom toolbar ────────────────────────────────────────────────
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.setBackground(Theme.BG_DARK);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        JButton editBtn   = UI.button("✎ Edit",         Theme.BG_ELEVATED);
        JButton doneBtn   = UI.button("✔ Mark Done",    Theme.ACCENT_GREEN);
        JButton removeBtn = UI.button("✕ Remove",       Theme.BG_ELEVATED);
        JButton undoBtn   = UI.button("↩ Undo",         Theme.BG_ELEVATED);
        JButton redoBtn   = UI.button("↪ Redo",         Theme.BG_ELEVATED);

        removeBtn.setForeground(Theme.ACCENT_RED);
        undoBtn.setForeground(Theme.TEXT_SECONDARY);
        redoBtn.setForeground(Theme.TEXT_SECONDARY);

        editBtn.addActionListener(e   -> { Task t = selected(); if (t != null) onEditTask.accept(t); });
        doneBtn.addActionListener(e   -> markStatus(Task.Status.DONE));
        removeBtn.addActionListener(e -> deleteSelected());
        undoBtn.addActionListener(e   -> { cmdMgr.undo(); refresh(); });
        redoBtn.addActionListener(e   -> { cmdMgr.redo(); refresh(); });

        // Restrict destructive actions to admin
        if (!currentUser.isAdmin()) removeBtn.setEnabled(false);

        bottom.add(editBtn); bottom.add(doneBtn); bottom.add(removeBtn);
        bottom.add(Box.createHorizontalStrut(16));
        bottom.add(undoBtn); bottom.add(redoBtn);
        add(bottom, BorderLayout.SOUTH);

        // Double-click to edit
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { Task t = selected(); if (t != null) onEditTask.accept(t); }
            }
        });
    }

    private void styleTable() {
        table.setFont(Theme.FONT_BODY);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setBackground(Theme.BG_DARK);
        table.setSelectionBackground(Theme.BG_HOVER);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setGridColor(Theme.BORDER);
        table.setRowHeight(36);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(12, 0));
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);

        JTableHeader th = table.getTableHeader();
        th.setBackground(Theme.BG_SIDEBAR);
        th.setForeground(Theme.TEXT_MUTED);
        th.setFont(Theme.FONT_LABEL);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Column widths
        int[] widths = {40, 200, 80, 100, 90, 100, 100, 80};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Priority renderer
        table.getColumnModel().getColumn(3).setCellRenderer(badgeRenderer(v ->
                Theme.priorityColor(Task.Priority.valueOf(v))));

        // Status renderer
        table.getColumnModel().getColumn(4).setCellRenderer(badgeRenderer(v ->
                Theme.statusColor(Task.Status.valueOf(v.replace(" ","_")))));

        // Progress renderer
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                int pct = val instanceof Integer ? (Integer) val : 0;
                JProgressBar pb = UI.progressBar(pct, Theme.ACCENT_BLUE);
                pb.setBackground(sel ? Theme.BG_HOVER : Theme.BG_DARK);
                return pb;
            }
        });
    }

    private TableCellRenderer badgeRenderer(java.util.function.Function<String, Color> colorFn) {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                String s = val != null ? val.toString() : "";
                JLabel badge = UI.badge(s.replace("_"," "), colorFn.apply(s));
                badge.setBackground(sel ? Theme.BG_HOVER : Theme.BG_DARK);
                badge.setOpaque(true);
                return badge;
            }
        };
    }

    public void refresh() {
        List<Task> all      = repo.getAllTasks();
        FilterStrategy strat = (FilterStrategy) filterBox.getSelectedItem();
        List<Task> filtered  = strat != null ? strat.filter(all) : all;

        String q = searchField.getText().trim().toLowerCase();
        if (!q.isEmpty() && !q.startsWith("🔍") && !q.startsWith("  🔍")) {
            filtered = filtered.stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(q)
                              || t.getCategory().toString().toLowerCase().contains(q)
                              || (t.getAssignedTo() != null && t.getAssignedTo().toLowerCase().contains(q)))
                    .toList();
        }

        tableModel.setTasks(filtered);
        countLabel.setText(filtered.size() + " of " + all.size() + " tasks");
    }

    @Override public void onTaskChanged(String event, Task task) { refresh(); }

    private Task selected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return tableModel.getTaskAt(table.convertRowIndexToModel(row));
    }

    private void markStatus(Task.Status s) {
        Task t = selected();
        if (t == null) { showNoSel(); return; }
        cmdMgr.execute(CommandManager.updateStatus(repo, t.getId(), s));
    }

    private void deleteSelected() {
        Task t = selected();
        if (t == null) { showNoSel(); return; }
        int ans = JOptionPane.showConfirmDialog(this,
                "Remove task: \"" + t.getTitle() + "\"?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.YES_OPTION)
            cmdMgr.execute(CommandManager.removeTask(repo, t.getId()));
    }

    private void showNoSel() {
        JOptionPane.showMessageDialog(this, "Please select a task first.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Inner table model ─────────────────────────────────────────────────────

    static class TaskTableModel extends AbstractTableModel {
        private static final String[] COLS = {"#","Title","Category","Priority","Status","Assignee","Progress","Due Date"};
        private java.util.List<Task> tasks = new java.util.ArrayList<>();

        void setTasks(java.util.List<Task> t) { this.tasks = new java.util.ArrayList<>(t); fireTableDataChanged(); }
        Task getTaskAt(int row) { return row >= 0 && row < tasks.size() ? tasks.get(row) : null; }

        @Override public int getRowCount()    { return tasks.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }
        @Override public Class<?> getColumnClass(int c) { return c == 0 || c == 6 ? Integer.class : String.class; }

        @Override public Object getValueAt(int r, int c) {
            Task t = tasks.get(r);
            return switch (c) {
                case 0 -> t.getId();
                case 1 -> t.getTitle();
                case 2 -> t.getCategory().toString();
                case 3 -> t.getPriority().toString();
                case 4 -> t.getStatus().toString();
                case 5 -> t.getAssignedTo() != null ? t.getAssignedTo() : "";
                case 6 -> t.getProgress();
                case 7 -> t.getDueDate() != null ? t.getDueDate().toString() : "—";
                default -> "";
            };
        }
    }
}
