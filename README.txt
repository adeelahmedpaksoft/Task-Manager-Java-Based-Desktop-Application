====================================================
 TaskManager Pro v2 — SET11103 Coursework
====================================================

HOW TO RUN IN ECLIPSE
----------------------
1. File → Import → General → Existing Projects into Workspace
2. Browse to this folder → Finish
3. Right-click src/taskmanager/Main.java → Run As → Java Application

OR run the JAR directly:
  java -jar TaskManagerV2.jar

====================================================
LOGIN CREDENTIALS
====================================================
  Role      | Username | Password
  ----------|----------|----------
  Admin     | admin    | admin123
  Developer | dev1     | dev123
  Developer | dev2     | dev456

Admin can: add/edit/delete tasks, view settings, manage all tasks
Developer can: view tasks, update status and progress, mark done

====================================================
FEATURES
====================================================
  ✦ Animated dark-theme login screen
  ✦ Role-based access (Admin / Developer)
  ✦ Interactive Dashboard with animated stats + charts
  ✦ Full Task List with search, filters, and inline badges
  ✦ Kanban Board (4 columns: Todo → In Progress → Review → Done)
  ✦ Interactive To-Do List with checkboxes and progress sliders
  ✦ Settings panel (Admin only)
  ✦ Undo / Redo support on all actions
  ✦ File persistence (CSV) + config (.properties)
  ✦ Activity log written to activity.log

====================================================
DESIGN PATTERNS
====================================================
  Singleton  → AppConfig (shared config, persisted)
  Observer   → TaskObserver: Dashboard, TaskList, Kanban, ToDo, Logger all auto-refresh
  Strategy   → 8 interchangeable filter strategies (All, My Tasks, Overdue, etc.)
  Command    → Add/Remove/UpdateStatus/UpdateProgress with full Undo/Redo

====================================================
