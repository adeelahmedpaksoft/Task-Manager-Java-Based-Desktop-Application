package taskmanager.model;

import taskmanager.observer.TaskObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In-memory task store. Subject in the Observer pattern.
 */
public class TaskRepository {

    private final List<Task>         tasks     = new ArrayList<>();
    private final List<TaskObserver> observers = new ArrayList<>();
    private int nextId = 1;

    public void addObserver(TaskObserver o)    { observers.add(o); }
    public void removeObserver(TaskObserver o) { observers.remove(o); }

    private void notify(String event, Task task) {
        for (TaskObserver o : observers) o.onTaskChanged(event, task);
    }

    public Task addTask(String title, String description,
                        Task.Priority priority, Task.Category category,
                        java.time.LocalDate dueDate, String assignedTo) {
        Task t = new Task(nextId++, title, description, priority, category, dueDate, assignedTo);
        tasks.add(t);
        notify("ADDED", t);
        return t;
    }

    public boolean removeTask(int id) {
        Task t = findById(id);
        if (t == null) return false;
        tasks.remove(t);
        notify("REMOVED", t);
        return true;
    }

    public boolean updateTask(Task updated) {
        Task ex = findById(updated.getId());
        if (ex == null) return false;
        ex.setTitle(updated.getTitle());
        ex.setDescription(updated.getDescription());
        ex.setPriority(updated.getPriority());
        ex.setStatus(updated.getStatus());
        ex.setCategory(updated.getCategory());
        ex.setDueDate(updated.getDueDate());
        ex.setAssignedTo(updated.getAssignedTo());
        ex.setProgress(updated.getProgress());
        ex.setTags(updated.getTags());
        notify("UPDATED", ex);
        return true;
    }

    public Task findById(int id) {
        return tasks.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
    }

    public List<Task> getAllTasks()  { return new ArrayList<>(tasks); }

    public List<Task> getByStatus(Task.Status s) {
        return tasks.stream().filter(t -> t.getStatus() == s).collect(Collectors.toList());
    }

    public long countByStatus(Task.Status s) {
        return tasks.stream().filter(t -> t.getStatus() == s).count();
    }

    public long countByPriority(Task.Priority p) {
        return tasks.stream().filter(t -> t.getPriority() == p).count();
    }

    public long countOverdue() {
        return tasks.stream().filter(Task::isOverdue).count();
    }

    public void setTasks(List<Task> loaded) {
        tasks.clear();
        tasks.addAll(loaded);
        nextId = tasks.stream().mapToInt(Task::getId).max().orElse(0) + 1;
        notify("LOADED", null);
    }

    public int size() { return tasks.size(); }
}
