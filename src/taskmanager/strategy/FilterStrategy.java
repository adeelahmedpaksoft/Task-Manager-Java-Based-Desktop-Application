package taskmanager.strategy;

import taskmanager.model.Task;
import java.util.List;

/** Strategy Pattern – contract for task filtering. */
public interface FilterStrategy {
    List<Task> filter(List<Task> tasks);
    String getName();
    String getIcon();
}
