package be.theking90000.mclib2.inject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyGraph {

    private final Map<Object, DependencyNode<?>> dependencies = new ConcurrentHashMap<>();

    public synchronized <T1, T2> void addDependency(T1 from, T2 to) {
        if (from == to)
            throw new IllegalArgumentException("Cannot add self-dependency");

        DependencyNode<T1> fromNode = getNode(from);
        DependencyNode<T2> toNode = getNode(to);

        fromNode.dependencies.add(toNode);
        toNode.dependents.add(fromNode);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> void close(T object) {
        DependencyNode<T> node = (DependencyNode<T>) dependencies.get(object);

        if (node == null)
            throw new IllegalArgumentException("Object not registered in the dependency graph");

        node.close();
    }

    public synchronized void close() {
        for (DependencyNode<?> node : new HashSet<>(dependencies.values())) {
            node.close();
        }
    }

    @SuppressWarnings("unchecked")
    private  <T> DependencyNode<T> getNode(T object) {
        return (DependencyNode<T>) dependencies.computeIfAbsent(object, DependencyNode::new);
    }

    private <T> void removeNode(T object) {
        dependencies.remove(object);
    }

    public String debug() {
        StringBuilder sb = new StringBuilder();
        sb.append("flowchart TD").append('\n');
        for(DependencyNode<?> node : dependencies.values()) {
            sb.append(node.debug());
        }

        return sb.toString();
    }

    private class DependencyNode<T> {
        private final T instance;

        private final Set<DependencyNode<?>> dependencies = new HashSet<>();
        private final Set<DependencyNode<?>> dependents = new HashSet<>();

        public DependencyNode(T instance) {
            this.instance = instance;
        }

        private void dispose() {
            if (instance instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) instance).close();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to close instance of type " + instance.getClass(), e);
                }
            } else if (instance instanceof Disposable) {
                ((Disposable) instance).dispose();
            }
        }

        public void close() {
            if (!dependents.isEmpty())
                return;

            for (DependencyNode<?> dependency : dependencies) {
                dependency.dependents.remove(this);
                dependency.close();
            }
            dependencies.clear();

            dispose();
            removeNode(instance);
        }

        private String toUniqueString() {
            return instance.getClass().getName() + "$" + Integer.toHexString(System.identityHashCode(instance));
        }

        public String debug() {
            StringBuilder sb = new StringBuilder();
            sb.append("    ").append(this.toUniqueString()).append('\n');
            for (DependencyNode<?> dependency : dependencies) {
                sb.append("    ")
                        //.append("\"")
                        .append(this.toUniqueString())
                        //.append("\"")
                         .append(" --> ")
                        //.append("\"")
                 .append(dependency.toUniqueString())
                // ).append("\""
                         .append("\n");
            }

            return sb.toString();
        }
    }
}
