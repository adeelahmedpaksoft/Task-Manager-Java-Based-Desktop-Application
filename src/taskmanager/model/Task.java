package taskmanager.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Core task data model.
 */
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Status   { TODO, IN_PROGRESS, REVIEW, DONE }
    public enum Category { FEATURE, BUG, IMPROVEMENT, DOCUMENTATION, TESTING, OTHER }

    private int       id;
    private String    title;
    private String    description;
    private Priority  priority;
    private Status    status;
    private Category  category;
    private LocalDate dueDate;
    private LocalDate createdDate;
    private String    assignedTo;  // username
    private int       progress;    // 0-100
    private String    tags;

    public Task(int id, String title, String description,
                Priority priority, Category category,
                LocalDate dueDate, String assignedTo) {
        this.id          = id;
        this.title       = title;
        this.description = description;
        this.priority    = priority;
        this.category    = category;
        this.dueDate     = dueDate;
        this.assignedTo  = assignedTo;
        this.status      = Status.TODO;
        this.createdDate = LocalDate.now();
        this.progress    = 0;
        this.tags        = "";
    }

    // Getters
    public int       getId()          { return id; }
    public String    getTitle()       { return title; }
    public String    getDescription() { return description; }
    public Priority  getPriority()    { return priority; }
    public Status    getStatus()      { return status; }
    public Category  getCategory()    { return category; }
    public LocalDate getDueDate()     { return dueDate; }
    public LocalDate getCreatedDate() { return createdDate; }
    public String    getAssignedTo()  { return assignedTo; }
    public int       getProgress()    { return progress; }
    public String    getTags()        { return tags; }

    // Setters
    public void setTitle(String v)       { this.title = v; }
    public void setDescription(String v) { this.description = v; }
    public void setPriority(Priority v)  { this.priority = v; }
    public void setStatus(Status v)      { this.status = v; }
    public void setCategory(Category v)  { this.category = v; }
    public void setDueDate(LocalDate v)  { this.dueDate = v; }
    public void setAssignedTo(String v)  { this.assignedTo = v; }
    public void setProgress(int v)       { this.progress = Math.max(0, Math.min(100, v)); }
    public void setTags(String v)        { this.tags = v; }
    public void setCreatedDate(LocalDate v){ this.createdDate = v; }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && status != Status.DONE;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s | %s%%", id, title, priority, status, progress);
    }
}
