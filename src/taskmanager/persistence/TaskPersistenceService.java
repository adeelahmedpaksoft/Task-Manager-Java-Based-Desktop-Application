package taskmanager.persistence;

import taskmanager.model.Task;
import taskmanager.singleton.AppConfig;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/** File-based CSV persistence for tasks. */
public class TaskPersistenceService {

    private final String path;
    public TaskPersistenceService() { this.path = AppConfig.getInstance().getDataFilePath(); }

    public void save(List<Task> tasks) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("id,title,description,priority,status,category,dueDate,assignedTo,progress,tags,createdDate");
            for (Task t : tasks) {
                pw.printf("%d,%s,%s,%s,%s,%s,%s,%s,%d,%s,%s%n",
                        t.getId(), esc(t.getTitle()), esc(t.getDescription()),
                        t.getPriority(), t.getStatus(), t.getCategory(),
                        t.getDueDate() != null ? t.getDueDate() : "",
                        esc(t.getAssignedTo() != null ? t.getAssignedTo() : ""),
                        t.getProgress(), esc(t.getTags() != null ? t.getTags() : ""),
                        t.getCreatedDate() != null ? t.getCreatedDate() : "");
            }
        }
    }

    public List<Task> load() throws IOException {
        List<Task> tasks = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) return tasks;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                try { Task t = parse(line); if (t != null) tasks.add(t); }
                catch (Exception e) { System.err.println("Skip: " + line); }
            }
        }
        return tasks;
    }

    private Task parse(String line) {
        String[] p = line.split(",", 11);
        if (p.length < 11) return null;
        int id = Integer.parseInt(p[0].trim());
        Task t = new Task(id, unesc(p[1]), unesc(p[2]),
                Task.Priority.valueOf(p[3].trim()),
                Task.Category.valueOf(p[5].trim()),
                p[6].isBlank() ? null : LocalDate.parse(p[6].trim()),
                unesc(p[7]));
        t.setStatus(Task.Status.valueOf(p[4].trim()));
        t.setProgress(Integer.parseInt(p[8].trim()));
        t.setTags(unesc(p[9]));
        if (!p[10].isBlank()) t.setCreatedDate(LocalDate.parse(p[10].trim()));
        return t;
    }

    private String esc(String s)   { return s == null ? "" : s.replace(",","\\,").replace("\n","\\n"); }
    private String unesc(String s) { return s == null ? "" : s.replace("\\,",",").replace("\\n","\n"); }
}
