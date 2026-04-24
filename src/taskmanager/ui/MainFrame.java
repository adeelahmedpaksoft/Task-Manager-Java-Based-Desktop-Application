package taskmanager.ui;

import taskmanager.auth.AuthService;
import taskmanager.command.CommandManager;
import taskmanager.model.Task;
import taskmanager.model.TaskRepository;
import taskmanager.model.User;
import taskmanager.observer.ActivityLogger;
import taskmanager.persistence.TaskPersistenceService;
import taskmanager.singleton.AppConfig;
import taskmanager.ui.components.Theme;
import taskmanager.ui.components.UI;
import taskmanager.ui.dashboard.DashboardPanel;
import taskmanager.ui.login.LoginPanel;
import taskmanager.ui.todo.TodoPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Root application frame.
 * Hosts the login screen and post-login workspace.
 */
public class MainFrame extends JFrame {

    private final TaskRepository       repo        = new TaskRepository();
    private final CommandManager       cmdMgr      = new CommandManager();
    private final TaskPersistenceService persistence = new TaskPersistenceService();
    private final ActivityLogger       logger       = new ActivityLogger();

    private JPanel rootPanel;   // CardLayout root
    private CardLayout rootLayout;

    // Post-login components (created after login)
    private SidebarPanel    sidebar;
    private DashboardPanel  dashPanel;
    private TaskListPanel   taskListPanel;
    private KanbanPanel     kanbanPanel;
    private TodoPanel       todoPanel;
    private SettingsPanel   settingsPanel;
    private JPanel          workspaceContent; // CardLayout inner content

    private static final String CARD_LOGIN     = "LOGIN";
    private static final String CARD_APP       = "APP";
    private static final String PAGE_DASHBOARD = "DASHBOARD";
    private static final String PAGE_TASKS     = "TASKS";
    private static final String PAGE_KANBAN    = "KANBAN";
    private static final String PAGE_TODO      = "TODO";
    private static final String PAGE_SETTINGS  = "SETTINGS";

    public MainFrame() {
        super("TaskManager Pro — SET11103");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { onExit(); }
        });

        repo.addObserver(logger);
        loadData();
        buildRoot();
    }

    // ── Root structure ────────────────────────────────────────────────────────

    private void buildRoot() {
        rootLayout = new CardLayout();
        rootPanel  = new JPanel(rootLayout);
        rootPanel.setBackground(Theme.BG_DARKEST);

        // Login screen
        LoginPanel loginPanel = new LoginPanel(this::onLogin);
        rootPanel.add(loginPanel, CARD_LOGIN);

        setContentPane(rootPanel);
        rootLayout.show(rootPanel, CARD_LOGIN);
    }

    // ── Login callback ────────────────────────────────────────────────────────

    private void onLogin(User user) {
        // Build workspace for this user
        buildWorkspace(user);
        rootPanel.add(buildWorkspacePanel(user), CARD_APP);
        rootLayout.show(rootPanel, CARD_APP);
        setTitle("TaskManager Pro — " + user.getDisplayName() + " (" + user.getRole() + ")");

        // Seed demo tasks if empty
        if (repo.size() == 0) seedDemoTasks(user);
        refreshAllPanels();
    }

    private void buildWorkspace(User user) {
        workspaceContent = new JPanel(new CardLayout());
        workspaceContent.setBackground(Theme.BG_DARK);

        dashPanel     = new DashboardPanel(repo, user);
        taskListPanel = new TaskListPanel(repo, cmdMgr, user, this::showAddTask, this::showEditTask);
        kanbanPanel   = new KanbanPanel(repo, cmdMgr, user, () -> {});
        todoPanel     = new TodoPanel(repo, cmdMgr, user);
        settingsPanel = new SettingsPanel();

        // Register observers
        repo.addObserver(dashPanel);
        repo.addObserver(taskListPanel);
        repo.addObserver(kanbanPanel);
        repo.addObserver(todoPanel);

        workspaceContent.add(dashPanel,     PAGE_DASHBOARD);
        workspaceContent.add(taskListPanel, PAGE_TASKS);
        workspaceContent.add(kanbanPanel,   PAGE_KANBAN);
        workspaceContent.add(todoPanel,     PAGE_TODO);
        workspaceContent.add(settingsPanel, PAGE_SETTINGS);
    }

    private JPanel buildWorkspacePanel(User user) {
        sidebar = new SidebarPanel(user, this::navigateTo, this::onLogout);

        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(Theme.BG_DARK);
        workspace.add(sidebar,          BorderLayout.WEST);
        workspace.add(workspaceContent, BorderLayout.CENTER);
        return workspace;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void navigateTo(SidebarPanel.Page page) {
        CardLayout cl = (CardLayout) workspaceContent.getLayout();
        switch (page) {
            case DASHBOARD -> cl.show(workspaceContent, PAGE_DASHBOARD);
            case TASKS     -> cl.show(workspaceContent, PAGE_TASKS);
            case KANBAN    -> cl.show(workspaceContent, PAGE_KANBAN);
            case TODO      -> cl.show(workspaceContent, PAGE_TODO);
            case SETTINGS  -> cl.show(workspaceContent, PAGE_SETTINGS);
        }
    }

    // ── Task dialogs ──────────────────────────────────────────────────────────

    private void showAddTask() {
        TaskFormDialog dlg = new TaskFormDialog(this);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            Task t = repo.addTask(
                    dlg.getTaskTitle(), dlg.getTaskDesc(),
                    dlg.getTaskPriority(), dlg.getTaskCategory(),
                    dlg.getTaskDueDate(), dlg.getTaskAssignee());
            t.setProgress(dlg.getTaskProgress());
            t.setTags(dlg.getTaskTags());
            repo.updateTask(t);
        }
    }

    private void showEditTask(Task task) {
        TaskFormDialog dlg = new TaskFormDialog(this, task);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            task.setTitle(dlg.getTaskTitle());
            task.setDescription(dlg.getTaskDesc());
            task.setPriority(dlg.getTaskPriority());
            task.setStatus(dlg.getTaskStatus());
            task.setCategory(dlg.getTaskCategory());
            task.setDueDate(dlg.getTaskDueDate());
            task.setAssignedTo(dlg.getTaskAssignee());
            task.setProgress(dlg.getTaskProgress());
            task.setTags(dlg.getTaskTags());
            repo.updateTask(task);
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    private void onLogout() {
        int ans = JOptionPane.showConfirmDialog(this,
                "Save your work and logout?", "Logout",
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (ans == JOptionPane.CANCEL_OPTION) return;
        if (ans == JOptionPane.YES_OPTION) saveData();

        AuthService.getInstance().logout();
        // Remove all workspace observers
        repo.removeObserver(dashPanel);
        repo.removeObserver(taskListPanel);
        repo.removeObserver(kanbanPanel);
        repo.removeObserver(todoPanel);

        // Rebuild login
        rootPanel.removeAll();
        LoginPanel lp = new LoginPanel(this::onLogin);
        rootPanel.add(lp, CARD_LOGIN);
        rootLayout.show(rootPanel, CARD_LOGIN);
        setTitle("TaskManager Pro — SET11103");
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    private void loadData() {
        try {
            List<Task> tasks = persistence.load();
            if (!tasks.isEmpty()) repo.setTasks(tasks);
        } catch (IOException e) {
            System.out.println("No saved data found, starting fresh.");
        }
    }

    private void saveData() {
        try {
            persistence.save(repo.getAllTasks());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not save: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAllPanels() {
        if (dashPanel     != null) dashPanel.refresh();
        if (taskListPanel != null) taskListPanel.refresh();
        if (kanbanPanel   != null) kanbanPanel.refresh();
        if (todoPanel     != null) todoPanel.refresh();
    }

    private void onExit() {
        int ans = JOptionPane.showConfirmDialog(this,
                "Save tasks before exiting?", "Exit",
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (ans == JOptionPane.CANCEL_OPTION) return;
        if (ans == JOptionPane.YES_OPTION) saveData();
        System.exit(0);
    }

    // ── Demo data seed ────────────────────────────────────────────────────────

    private void seedDemoTasks(User user) {
        java.time.LocalDate today = java.time.LocalDate.now();

        // Add some realistic demo tasks
        repo.addTask("Design login UI mockup", "Create Figma mockups for the new login flow",
                Task.Priority.HIGH, Task.Category.FEATURE, today.plusDays(3), "dev1");

        Task t2 = repo.addTask("Fix null pointer in TaskRepository", "Occurs when task list is empty on load",
                Task.Priority.CRITICAL, Task.Category.BUG, today.minusDays(1), user.getUsername());
        t2.setStatus(Task.Status.IN_PROGRESS); t2.setProgress(45); repo.updateTask(t2);

        Task t3 = repo.addTask("Write unit tests for AuthService", "Cover login, hashing and role checks",
                Task.Priority.MEDIUM, Task.Category.TESTING, today.plusDays(7), "dev2");
        t3.setStatus(Task.Status.TODO); t3.setProgress(0); repo.updateTask(t3);

        Task t4 = repo.addTask("Update API documentation", "Reflect new endpoint changes in README",
                Task.Priority.LOW, Task.Category.DOCUMENTATION, today.plusDays(14), "dev1");
        t4.setStatus(Task.Status.DONE); t4.setProgress(100); repo.updateTask(t4);

        Task t5 = repo.addTask("Implement CSV export", "Allow users to export task list to CSV",
                Task.Priority.MEDIUM, Task.Category.FEATURE, today.plusDays(5), user.getUsername());
        t5.setStatus(Task.Status.IN_PROGRESS); t5.setProgress(65); repo.updateTask(t5);

        Task t6 = repo.addTask("Refactor filter strategy classes", "Reduce duplication using abstract base",
                Task.Priority.LOW, Task.Category.IMPROVEMENT, today.plusDays(10), "dev2");
        t6.setStatus(Task.Status.REVIEW); t6.setProgress(90); repo.updateTask(t6);

        Task t7 = repo.addTask("Overdue: Migrate database schema", "Update schema to v3 format",
                Task.Priority.HIGH, Task.Category.OTHER, today.minusDays(3), "dev1");
        t7.setStatus(Task.Status.TODO); repo.updateTask(t7);

        Task t8 = repo.addTask("Set up CI/CD pipeline", "Configure GitHub Actions for auto-deploy",
                Task.Priority.HIGH, Task.Category.FEATURE, today.plusDays(2), user.getUsername());
        t8.setStatus(Task.Status.IN_PROGRESS); t8.setProgress(30); repo.updateTask(t8);
    }
}
