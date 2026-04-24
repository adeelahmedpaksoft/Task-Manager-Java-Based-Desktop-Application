package taskmanager.ui;

import taskmanager.auth.AuthService;
import taskmanager.model.Task;
import taskmanager.ui.components.Theme;
import taskmanager.ui.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Dark-themed modal dialog for adding or editing a task.
 */
public class TaskFormDialog extends JDialog {

    private JTextField  titleField;
    private JTextArea   descArea;
    private JComboBox<Task.Priority> priorityBox;
    private JComboBox<Task.Status>   statusBox;
    private JComboBox<Task.Category> categoryBox;
    private JComboBox<String>        assigneeBox;
    private JTextField  dueDateField;
    private JSlider     progressSlider;
    private JLabel      progressLabel;
    private JTextField  tagsField;

    private boolean confirmed = false;
    private final Task editTask;

    public TaskFormDialog(JFrame parent) {
        super(parent, "New Task", true);
        this.editTask = null;
        build();
    }

    public TaskFormDialog(JFrame parent, Task task) {
        super(parent, "Edit Task — " + task.getTitle(), true);
        this.editTask = task;
        build();
        populate(task);
    }

    private void build() {
        setUndecorated(false);
        setSize(520, 620);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(Theme.BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(root);

        // ── Title bar ──────────────────────────────────────────────────────
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Theme.BG_SIDEBAR);
        titleBar.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel titleLbl = UI.label(editTask == null ? "Create New Task" : "Edit Task",
                Theme.FONT_HEADING, Theme.TEXT_PRIMARY);
        JLabel sub = UI.label(editTask == null ? "Fill in the details below" : "Update task information",
                Theme.FONT_SMALL, Theme.TEXT_MUTED);
        JPanel tCol = new JPanel(); tCol.setLayout(new BoxLayout(tCol, BoxLayout.Y_AXIS)); tCol.setOpaque(false);
        tCol.add(titleLbl); tCol.add(Box.createVerticalStrut(3)); tCol.add(sub);
        titleBar.add(tCol, BorderLayout.WEST);
        root.add(titleBar, BorderLayout.NORTH);

        // ── Form ──────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_DARK);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 0, 4, 0);
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.gridx  = 0; gc.weightx = 1;

        int y = 0;

        // Title
        gc.gridy = y++; form.add(fieldLabel("Title *"), gc);
        gc.gridy = y++; titleField = UI.textField(""); titleField.setPreferredSize(new Dimension(0, 40)); form.add(titleField, gc);

        // Row: Category + Priority
        gc.gridy = y++;
        JPanel row1 = twoCol(
                fieldLabel("Category"), categoryBox = UI.combo(Task.Category.values()),
                fieldLabel("Priority"), priorityBox  = UI.combo(Task.Priority.values()));
        form.add(row1, gc);

        // Row: Status + Assignee
        gc.gridy = y++;
        List<String> usernames = AuthService.getInstance().getUsernames();
        usernames.add(0, "(unassigned)");
        assigneeBox = UI.combo(usernames.toArray(new String[0]));
        JPanel row2 = twoCol(
                fieldLabel("Status"), statusBox = UI.combo(Task.Status.values()),
                fieldLabel("Assignee"), assigneeBox);
        form.add(row2, gc);

        // Due date + Tags
        gc.gridy = y++;
        dueDateField = UI.textField(LocalDate.now().plusDays(7).toString());
        tagsField    = UI.textField("e.g. frontend, urgent");
        JPanel row3 = twoCol(fieldLabel("Due Date (YYYY-MM-DD)"), dueDateField,
                             fieldLabel("Tags"), tagsField);
        form.add(row3, gc);

        // Progress
        gc.gridy = y++; gc.insets = new Insets(10, 0, 4, 0);
        JPanel progHeader = new JPanel(new BorderLayout()); progHeader.setOpaque(false);
        progHeader.add(fieldLabel("Progress"), BorderLayout.WEST);
        progressLabel = UI.label("0%", Theme.FONT_SUBHEAD, Theme.ACCENT_BLUE);
        progHeader.add(progressLabel, BorderLayout.EAST);
        form.add(progHeader, gc);

        gc.gridy = y++; gc.insets = new Insets(0, 0, 10, 0);
        progressSlider = new JSlider(0, 100, 0);
        progressSlider.setBackground(Theme.BG_DARK);
        progressSlider.setForeground(Theme.ACCENT_BLUE);
        progressSlider.setMajorTickSpacing(25); progressSlider.setPaintTicks(true);
        progressSlider.addChangeListener(e -> progressLabel.setText(progressSlider.getValue() + "%"));
        form.add(progressSlider, gc);

        // Description
        gc.gridy = y++; gc.insets = new Insets(4, 0, 4, 0);
        form.add(fieldLabel("Description"), gc);
        gc.gridy = y++; gc.weighty = 1; gc.fill = GridBagConstraints.BOTH;
        descArea = new JTextArea(4, 0);
        descArea.setFont(Theme.FONT_BODY);
        descArea.setForeground(Theme.TEXT_PRIMARY);
        descArea.setBackground(Theme.BG_ELEVATED);
        descArea.setCaretColor(Theme.ACCENT_BLUE);
        descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        descArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane ds = UI.scroll(descArea);
        ds.setPreferredSize(new Dimension(0, 80));
        form.add(ds, gc);

        root.add(form, BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btnPanel.setBackground(Theme.BG_SIDEBAR);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        JButton cancel = UI.button("Cancel",  Theme.BG_ELEVATED);
        JButton save   = UI.button(editTask == null ? "Create Task" : "Save Changes", Theme.ACCENT_BLUE);
        save.setPreferredSize(new Dimension(130, 38));
        cancel.setPreferredSize(new Dimension(90, 38));
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e   -> onSave());
        btnPanel.add(cancel); btnPanel.add(save);
        root.add(btnPanel, BorderLayout.SOUTH);

        // Style combo boxes
        for (JComboBox<?> cb : new JComboBox[]{priorityBox, statusBox, categoryBox, assigneeBox}) {
            cb.setBackground(Theme.BG_ELEVATED);
            cb.setForeground(Theme.TEXT_PRIMARY);
            cb.setFont(Theme.FONT_BODY);
            cb.setPreferredSize(new Dimension(0, 38));
        }
    }

    private void populate(Task t) {
        titleField.setText(t.getTitle());
        descArea.setText(t.getDescription());
        priorityBox.setSelectedItem(t.getPriority());
        statusBox.setSelectedItem(t.getStatus());
        categoryBox.setSelectedItem(t.getCategory());
        dueDateField.setText(t.getDueDate() != null ? t.getDueDate().toString() : "");
        tagsField.setText(t.getTags() != null ? t.getTags() : "");
        progressSlider.setValue(t.getProgress());
        progressLabel.setText(t.getProgress() + "%");
        if (t.getAssignedTo() != null) assigneeBox.setSelectedItem(t.getAssignedTo());
    }

    private void onSave() {
        if (titleField.getText().trim().isEmpty()) {
            titleField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.ACCENT_RED, 1, true),
                    new EmptyBorder(6, 10, 6, 10)));
            titleField.requestFocus();
            return;
        }
        confirmed = true;
        dispose();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public boolean             isConfirmed()     { return confirmed; }
    public String              getTaskTitle()     { return titleField.getText().trim(); }
    public String              getTaskDesc()      { return descArea.getText().trim(); }
    public Task.Priority       getTaskPriority()  { return (Task.Priority) priorityBox.getSelectedItem(); }
    public Task.Status         getTaskStatus()    { return (Task.Status)   statusBox.getSelectedItem(); }
    public Task.Category       getTaskCategory()  { return (Task.Category) categoryBox.getSelectedItem(); }
    public int                 getTaskProgress()  { return progressSlider.getValue(); }
    public String              getTaskTags()      { return tagsField.getText().trim(); }
    public String              getTaskAssignee()  {
        String a = (String) assigneeBox.getSelectedItem();
        return "(unassigned)".equals(a) ? "" : a;
    }
    public LocalDate getTaskDueDate() {
        String s = dueDateField.getText().trim();
        try { return s.isEmpty() ? null : LocalDate.parse(s); }
        catch (DateTimeParseException e) { return null; }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JLabel fieldLabel(String text) {
        JLabel l = UI.label(text, Theme.FONT_LABEL, Theme.TEXT_SECONDARY);
        l.setBorder(new EmptyBorder(2, 0, 2, 0));
        return l;
    }

    private JPanel twoCol(JLabel l1, JComponent c1, JLabel l2, JComponent c2) {
        JPanel p = new JPanel(new GridLayout(2, 2, 10, 4));
        p.setOpaque(false);
        p.add(l1); p.add(l2);
        p.add(c1); p.add(c2);
        return p;
    }
}
