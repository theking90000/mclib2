package be.theking90000.mclib2.platform;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PlatformStore {

    private static final ThreadLocal<Map<String, Object>> values = new ThreadLocal<>();

    public static void enter() {
        values.set(new HashMap<>());
    }

    public static void exit() {
        values.remove();
    }

    private static Map<String, Object> check() {
        Map<String, Object> map = values.get();
        if (map == null) {
            throw new IllegalStateException("Not in a PlatformRegistry context");
        }
        return map;
    }

    public static <T> T get(String key) {
        return (T) check().get(key);
    }

    public static <T> T put(String key, T value) {
        return (T) check().put(key, value);
    }

    public static <T> T remove(String key) {
        return (T) check().remove(key);
    }

    public static <T> T computeIfAbsent(String key, Supplier<T> supplier) {
        return (T) check().computeIfAbsent(key, k -> supplier.get());
    }

}
