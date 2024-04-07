package util.helpers.system;

public class MemoryUsageTracker {
    private static long maxMemoryUsed = 0;
    private static MemoryUsageTracker instance;
    private final static Runtime runtime = Runtime.getRuntime();

    private MemoryUsageTracker(){}

    public static synchronized MemoryUsageTracker getInstance() {
        if (instance == null) {
            instance = new MemoryUsageTracker();
        }
        return instance;
    }

    public void checkAndUpdateMaxMemoryUsage() {
        long currentlyUsedMemory = runtime.totalMemory() - runtime.freeMemory();
        if (currentlyUsedMemory > maxMemoryUsed) {
            maxMemoryUsed = currentlyUsedMemory;
        }
    }

    public void printMemoryUsageMax(){
        LoggerHelpers.instance().info("Max memory usage (Mo): "+maxMemoryUsed / (1024 * 1024));
    }
}
