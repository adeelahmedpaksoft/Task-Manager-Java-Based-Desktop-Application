package taskmanager.observer;

import taskmanager.model.Task;
import taskmanager.singleton.AppConfig;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Observer Pattern – logs every task event to a file. */
public class ActivityLogger implements TaskObserver {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onTaskChanged(String event, Task task) {
        String msg = String.format("[%s] %s | %s",
                LocalDateTime.now().format(FMT), event,
                task != null ? task.toString() : "ALL TASKS");
        System.out.println("LOG: " + msg);
        try (PrintWriter pw = new PrintWriter(new FileWriter(
                AppConfig.getInstance().getLogFilePath(), true))) {
            pw.println(msg);
        } catch (IOException e) {
            System.err.println("Log error: " + e.getMessage());
        }
    }
}
