package util.helpers;

public class IdGenerator {
    private int id = 0;

    private IdGenerator() {
    }

    private static IdGenerator instance = new IdGenerator();

    public static IdGenerator instance() {
        return instance;
    }

    public int nextId() {
        return id++;
    }

    public String nextId(String prefix) {
        return prefix + id++;
    }
}
