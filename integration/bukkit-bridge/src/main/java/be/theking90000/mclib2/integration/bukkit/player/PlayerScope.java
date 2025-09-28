package be.theking90000.mclib2.integration.bukkit.player;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Scopes;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerScope implements Scope {

    private final ThreadLocal<UUID> currentPlayer = new ThreadLocal<>();
    private final Map<UUID, Map<Key<?>, Object>> scopedObjects =
            new ConcurrentHashMap<>();

    private static final Provider<Object> SEEDED_KEY_PROVIDER = new Provider<Object>() {
        @Override
        public Object get() {
            throw new IllegalStateException("If you got here then it means that" +
                    " your code asked for scoped object which should have been" +
                    " explicitly seeded in this scope by calling" +
                    " seed(), but was not.");
        }
    };

    public void enter(UUID playerId) {
        currentPlayer.set(playerId);
        scopedObjects.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap<>());
    }

    public void exit() {
        currentPlayer.remove();
    }

    public void remove(UUID playerId) {
        scopedObjects.remove(playerId);
    }

    private Map<Key<?>, Object> getScopedObjects() {
        UUID playerId = currentPlayer.get();
        if (playerId == null) {
            throw new IllegalStateException("No player is currently in scope");
        }

        Map<Key<?>, Object> playerScope = scopedObjects.get(playerId);
        if (playerScope == null) {
            throw new IllegalStateException("No scope found for player: " + playerId);
        }

        return playerScope;
    }

    public <T> void seed(Key<T> key, T instance) {
        getScopedObjects().put(key, instance);
    }

    public <T> void seed(Class<T> clazz, T instance) {
        seed(Key.get(clazz), instance);
    }

    @Override
    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
        return new Provider<T>() {
            @Override
            public T get() {
                Map<Key<?>, Object> scopedObjects = getScopedObjects();
                if (scopedObjects.containsKey(key)) {
                    return (T) scopedObjects.get(key);
                }
                T instance = unscoped.get();

                if (Scopes.isCircularProxy(instance))
                    return instance;

                scopedObjects.put(key, instance);

                return instance;
            }
        };
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Provider<T> seededKeyProvider() {
        return (Provider<T>) SEEDED_KEY_PROVIDER;
    }
}
