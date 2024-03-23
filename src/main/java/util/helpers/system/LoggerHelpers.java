package util.helpers.system;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerHelpers {

    private LoggerHelpers() {
    }

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public static void info(String msg) {
        System.out.println("[INFO ] " + dtf.format(LocalDateTime.now()) + " " + msg);
    }

    public static void warning(String msg) {
        System.out.println("[WARN ] " + dtf.format(LocalDateTime.now()) + " " + msg);
    }

    public static void error(String msg) {
        System.out.println("[ERROR ] " + dtf.format(LocalDateTime.now()) + " " + msg);
    }

    public static void fatal(String msg) {
        System.out.println("[FATAL ] " + dtf.format(LocalDateTime.now()) + " " + msg);
    }
}
