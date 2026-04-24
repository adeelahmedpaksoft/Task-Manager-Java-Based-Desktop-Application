package taskmanager.command;

import taskmanager.model.Task;
import taskmanager.model.TaskRepository;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;

// ── Command interface ────────────────────────────────────────────────────────

// ── AddTaskCommand ───────────────────────────────────────────────────────────
class AddTaskCommand implements Command {
    private final TaskRepository repo;
    private final String title, description, assignedTo;
    private final Task.Priority priority;
    private final Task.Category category;
    private final LocalDate dueDate;
    private Task added;

    public AddTaskCommand(TaskRepository repo, String title, String description,
                          Task.Priority priority, Task.Category category,
                          LocalDate dueDate, String assignedTo) {
        this.repo = repo; this.title = title; this.description = description;
        this.priority = priority; this.category = category;
        this.dueDate = dueDate; this.assignedTo = assignedTo;
    }
    public void execute() { added = repo.addTask(title, description, priority, category, dueDate, assignedTo); }
    public void undo()    { if (added != null) repo.removeTask(added.getId()); }
    public String getDescription() { return "Add: " + title; }
}

// ── RemoveTaskCommand ────────────────────────────────────────────────────────
class RemoveTaskCommand implements Command {
    private final TaskRepository repo;
    private final int id;
    private Task snapshot;

    public RemoveTaskCommand(TaskRepository repo, int id) { this.repo = repo; this.id = id; }
    public void execute() {
        Task t = repo.findById(id);
        if (t != null) {
            snapshot = copy(t);
            repo.removeTask(id);
        }
    }
    public void undo() {
        if (snapshot != null)
            repo.addTask(snapshot.getTitle(), snapshot.getDescription(),
                    snapshot.getPriority(), snapshot.getCategory(),
                    snapshot.getDueDate(), snapshot.getAssignedTo());
    }
    public String getDescription() { return "Remove task #" + id; }
    private Task copy(Task t) {
        Task c = new Task(t.getId(), t.getTitle(), t.getDescription(),
                t.getPriority(), t.getCategory(), t.getDueDate(), t.getAssignedTo());
        c.setStatus(t.getStatus()); c.setProgress(t.getProgress());
        return c;
    }
}

// ── UpdateStatusCommand ──────────────────────────────────────────────────────
class UpdateStatusCommand implements Command {
    private final TaskRepository repo;
    private final int id;
    private final Task.Status newStatus;
    private Task.Status prev;

    public UpdateStatusCommand(TaskRepository repo, int id, Task.Status ns) {
        this.repo = repo; this.id = id; this.newStatus = ns;
    }
    public void execute() {
        Task t = repo.findById(id);
        if (t != null) { prev = t.getStatus(); t.setStatus(newStatus); repo.updateTask(t); }
    }
    public void undo() {
        if (prev != null) {
            Task t = repo.findById(id);
            if (t != null) { t.setStatus(prev); repo.updateTask(t); }
        }
    }
    public String getDescription() { return "Status -> " + newStatus + " (#" + id + ")"; }
}

// ── UpdateProgressCommand ────────────────────────────────────────────────────
class UpdateProgressCommand implements Command {
    private final TaskRepository repo;
    private final int id, newProgress;
    private int prevProgress;

    public UpdateProgressCommand(TaskRepository repo, int id, int progress) {
        this.repo = repo; this.id = id; this.newProgress = progress;
    }
    public void execute() {
        Task t = repo.findById(id);
        if (t != null) { prevProgress = t.getProgress(); t.setProgress(newProgress); repo.updateTask(t); }
    }
    public void undo() {
        Task t = repo.findById(id);
        if (t != null) { t.setProgress(prevProgress); repo.updateTask(t); }
    }
    public String getDescription() { return "Progress -> " + newProgress + "% (#" + id + ")"; }
}

// ── CommandManager ───────────────────────────────────────────────────────────
public class CommandManager {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command cmd) {
        cmd.execute(); undoStack.push(cmd); redoStack.clear();
    }
    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
    public String undoLabel() { return canUndo() ? undoStack.peek().getDescription() : ""; }
    public String redoLabel() { return canRedo() ? redoStack.peek().getDescription() : ""; }
    public void undo() { if (canUndo()) { Command c = undoStack.pop(); c.undo(); redoStack.push(c); } }
    public void redo() { if (canRedo()) { Command c = redoStack.pop(); c.execute(); undoStack.push(c); } }

    // Factory methods so callers don't need to import inner classes
    public static Command addTask(TaskRepository r, String title, String desc,
                                  Task.Priority p, Task.Category cat,
                                  java.time.LocalDate due, String assignee) {
        return new AddTaskCommand(r, title, desc, p, cat, due, assignee);
    }
    public static Command removeTask(TaskRepository r, int id) { return new RemoveTaskCommand(r, id); }
    public static Command updateStatus(TaskRepository r, int id, Task.Status s) { return new UpdateStatusCommand(r, id, s); }
    public static Command updateProgress(TaskRepository r, int id, int pct) { return new UpdateProgressCommand(r, id, pct); }
}
