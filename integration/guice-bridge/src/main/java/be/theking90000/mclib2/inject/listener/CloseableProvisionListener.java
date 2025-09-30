package be.theking90000.mclib2.inject.listener;

import be.theking90000.mclib2.inject.CloseableRegistry;
import com.google.inject.Key;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ProvisionListener;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CloseableProvisionListener implements ProvisionListener {

    private final CloseableRegistry registry;

    public CloseableProvisionListener(CloseableRegistry registry) {
        this.registry = registry;
    }

    private Set<Dependency<?>> collectDependencies(Key<?> key) {
        InjectionPoint ip = InjectionPoint.forConstructorOf(key.getTypeLiteral());
        Set<Dependency<?>> deps = new HashSet<>(ip.getDependencies());

        for(InjectionPoint pp : InjectionPoint.forInstanceMethodsAndFields(key.getTypeLiteral()))
            deps.addAll(pp.getDependencies());

        return deps;
    }

    @Override
    public <T> void onProvision(ProvisionInvocation<T> provision) {
        Map<Key<?>, ArrayDeque<Object>> provisionInstances = registry.getProvisionInstances();

        T instance = provision.provision();
        // Collect stack as direct dependencies of the provisioned instance
        for (Dependency<?> dep : collectDependencies(provision.getBinding().getKey())) {
            ArrayDeque<Object> deq = provisionInstances.get(dep.getKey());
            if (deq == null || deq.isEmpty())
                throw new IllegalStateException("No provisioned instance found for dependency " + dep.getKey() + " of " + provision.getBinding().getKey());
            Object o = deq.pop();
            if (deq.isEmpty())
                provisionInstances.remove(dep.getKey());

            registry.getDependencyGraph().addDependency(instance, o);
        }

        provisionInstances
                .computeIfAbsent(provision.getBinding().getKey(), (k)-> new ArrayDeque<>())
                .push(instance);
    }
}
