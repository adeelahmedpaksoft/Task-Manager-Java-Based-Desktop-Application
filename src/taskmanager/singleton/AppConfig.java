package taskmanager.singleton;

import java.io.*;
import java.util.Properties;

/**
 * Singleton Pattern – single shared application configuration.
 * Persists settings to a .properties file.
 */
public class AppConfig {

    private static AppConfig instance;
    private static final String CONFIG_FILE = "taskmanager_config.properties";
    private final Properties props = new Properties();

    private AppConfig() {
        File f = new File(CONFIG_FILE);
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) { props.load(fis); }
            catch (IOException ignored) {}
        } else {
            props.setProperty("data.file",   "tasks.dat");
            props.setProperty("log.file",    "activity.log");
            props.setProperty("app.theme",   "Dark");
            props.setProperty("app.version", "2.0");
            save();
        }
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) instance = new AppConfig();
        return instance;
    }

    public String getDataFilePath() { return props.getProperty("data.file",   "tasks.dat"); }
    public String getLogFilePath()  { return props.getProperty("log.file",    "activity.log"); }
    public String getTheme()        { return props.getProperty("app.theme",   "Dark"); }
    public String getVersion()      { return props.getProperty("app.version", "2.0"); }

    public void setTheme(String t)       { props.setProperty("app.theme", t); save(); }
    public void setDataFilePath(String p){ props.setProperty("data.file", p); save(); }

    public void save() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "TaskManager V2 Configuration");
        } catch (IOException ignored) {}
    }
}
