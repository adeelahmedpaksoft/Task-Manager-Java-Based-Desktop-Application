package taskmanager.command;

/** Command Pattern – base contract for all executable actions. */
public interface Command {
    void execute();
    void undo();
    String getDescription();
}
