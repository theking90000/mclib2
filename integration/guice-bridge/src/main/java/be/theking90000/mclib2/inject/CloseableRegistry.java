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

}
