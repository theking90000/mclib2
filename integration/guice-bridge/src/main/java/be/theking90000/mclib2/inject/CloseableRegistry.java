package be.theking90000.mclib2.inject;

import com.google.inject.Key;

import java.util.*;

public class CloseableRegistry {

    /**
     * Allows to track dependencies generated during provision.
     */
    private final ThreadLocal<Map<Key<?>, ArrayDeque<Object>>> provisionInstances =
            ThreadLocal.withInitial(HashMap::new);

    private final DependencyGraph dependencyGraph = new DependencyGraph() {
        @Override
        protected <T> void dispose(T instance) {
            callDisposeListeners(instance);
        }
    };

    private final Map<Key<?>, Object> singletonInstances = new HashMap<>();

    private final Set<DisposeListener> disposeListeners = new HashSet<>();

    private CloseableInjector injector;

    /**
     * Get the map of provisioned instances for the current thread.
     * @return Map of provisioned instances.
     */
    public Map<Key<?>, ArrayDeque<Object>> getProvisionInstances() {
        return provisionInstances.get();
    }

    public DependencyGraph getDependencyGraph() {
        return dependencyGraph;
    }

    public Map<Key<?>, Object> getSingletonInstances() {
        return singletonInstances;
    }

    protected void setInjector(CloseableInjector injector) {
        this.injector = injector;
    }

    protected Set<DisposeListener> getDisposeListeners() {
        return disposeListeners;
    }

    protected <T> void callDisposeListeners(T instance) {
        for (DisposeListener listener : disposeListeners) {
            try {
                listener.onDispose(instance);
            } catch (Exception e) {
                throw new RuntimeException("Error while calling dispose listener: " + listener, e);
            }
        }
    }

    public CloseableInjector getInjector() {
        return this.injector;
    }
}
