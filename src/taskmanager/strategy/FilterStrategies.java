package taskmanager.strategy;

import taskmanager.model.Task;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/** Strategy Pattern – contract for task filtering. */

class AllTasksStrategy implements FilterStrategy {
    public List<Task> filter(List<Task> t) { return t; }
    public String getName() { return "All Tasks"; }
    public String getIcon() { return "⊞"; }
}

class TodoStrategy implements FilterStrategy {
    public List<Task> filter(List<Task> t) {
        return t.stream().filter(x -> x.getStatus() == Task.Status.TODO).collect(Collectors.toList());
    }
    public String getName() { return "To Do"; }
    public String getIcon() { return "○"; }
}

class InProgressStrategy implements FilterStrategy {
    public List<Task> filter(List<Task> t) {
        return t.stream().filter(x -> x.getStatus() == Task.Status.IN_PROGRESS).collect(Collectors.toList());
    }
    public String getName() { return "In Progress"; }
    public String getIcon() { return "◑"; }
}

class ReviewStrategy implements FilterStrategy {
    public List<Task> filter(List<Task> t) {
        return t.stream().filter(x -> x.getStatus() == Task.Status.REVIEW).collect(Collectors.toList());
    }
    public String getName() { return "In Review"; }
    public String getIcon() { return "◷"; }
}

class DoneStrategy implements FilterStrategy {
    public List<Task> filter(List<Task> t) {
        return t.stream().filter(x -> x.getStatus() == Task.Status.DONE).collect(Collectors.toList());
    }
    public String getName() { return "Done"; }
    public String getIcon() { return "●"; }
}

class CriticalStrategy implements FilterStrategy {
    public List<Task> filter(List<Task> t) {
        return t.stream().filter(x -> x.getPriority() == Task.Priority.CRITICAL).collect(Collectors.toList());
    }
    public String getName() { return "Critical"; }
    public String getIcon() { return "!"; }
}

class OverdueStrategy implements FilterStrategy {
    public List<Task> filter(List<Task> t) {
        return t.stream().filter(Task::isOverdue).collect(Collectors.toList());
    }
    public String getName() { return "Overdue"; }
    public String getIcon() { return "⚠"; }
}

class MyTasksStrategy implements FilterStrategy {
    private final String username;
    public MyTasksStrategy(String username) { this.username = username; }
    public List<Task> filter(List<Task> t) {
        return t.stream().filter(x -> username.equalsIgnoreCase(x.getAssignedTo())).collect(Collectors.toList());
    }
    public String getName() { return "My Tasks"; }
    public String getIcon() { return "★"; }
}

/** Factory exposing all strategies. */
public class FilterStrategies {
    private FilterStrategies() {}
    public static FilterStrategy[] all(String username) {
        return new FilterStrategy[]{
            new AllTasksStrategy(),
            new MyTasksStrategy(username),
            new TodoStrategy(),
            new InProgressStrategy(),
            new ReviewStrategy(),
            new DoneStrategy(),
            new CriticalStrategy(),
            new OverdueStrategy()
        };
    }
}
