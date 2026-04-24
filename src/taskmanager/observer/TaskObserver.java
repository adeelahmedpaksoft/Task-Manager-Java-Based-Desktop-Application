package taskmanager.observer;

import taskmanager.model.Task;

/** Observer Pattern – observer contract. */
public interface TaskObserver {
    void onTaskChanged(String event, Task task);
}
