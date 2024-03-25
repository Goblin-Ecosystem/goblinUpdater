package util.helpers.system;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// TODO: why not using a regular Logger? - Cause of my thesis subject, I don't want to add new libraries just to make prints.
public class LoggerHelpers {

    public enum Level implements Comparable<Level> {
        LOW, INFO, WARN, ERROR, FATAL;
    }

    private Level level;

    private static LoggerHelpers instance = new LoggerHelpers(Level.INFO);

    public static LoggerHelpers instance() {
        return instance;
    }

    private LoggerHelpers(Level level) {
        this.level = level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public void low(String msg) {
        if (Level.LOW.compareTo(level)>=0)
            System.out.println("[LOW ] " + dtf.format(LocalDateTime.now()) + " " + msg);
    }

    public void info(String msg) {
        if (Level.INFO.compareTo(level)>=0)
            System.out.println("[INFO ] " + dtf.format(LocalDateTime.now()) + " " + msg);
    }

    public void warning(String msg) {
        if (Level.WARN.compareTo(level)>=0)
            System.out.println("[WARN ] " + dtf.format(LocalDateTime.now()) + " " + msg);
    }

    public void error(String msg) {
        if (Level.ERROR.compareTo(level)>=0)
            System.out.println("[ERROR ] " + dtf.format(LocalDateTime.now()) + " " + msg);
    }

    public void fatal(String msg) {
        if (Level.FATAL.compareTo(level)>=0)
            System.out.println("[FATAL ] " + dtf.format(LocalDateTime.now()) + " " + msg);
    }
}
