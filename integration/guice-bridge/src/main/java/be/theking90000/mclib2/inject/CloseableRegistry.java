package be.theking90000.mclib2.inject;

import com.google.inject.Key;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CloseableRegistry {

    /**
     * Allows to track dependencies generated during provision.
     */
    private final ThreadLocal<Map<Key<?>, ArrayDeque<Object>>> provisionInstances =
            ThreadLocal.withInitial(HashMap::new);

    private final DependencyGraph dependencyGraph = new DependencyGraph();

    private final Map<Key<?>, Object> singletonInstances = new HashMap<>();

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

    protected CloseableInjector getInjector() {
        return this.injector;
    }
}
