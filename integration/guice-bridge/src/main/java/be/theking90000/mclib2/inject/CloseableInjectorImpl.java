package be.theking90000.mclib2.inject;

import com.google.inject.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CloseableInjectorImpl extends DelegateInjector implements CloseableInjector {

    private final CloseableRegistry registry;

    protected CloseableInjectorImpl(Injector injector, CloseableRegistry registry) {
        super(injector);

        this.registry = registry;
    }

    public static CloseableInjector createInjector(Stage stage, Collection<Module> modules) {
        CloseableRegistry registry = new CloseableRegistry();

        Set<Module> m = new HashSet<>();
        m.add(new CloseableModule(registry));
        m.addAll(modules);

        for (Module mod : m) {
            if (mod instanceof AbstractCloseableModule) {
                ((AbstractCloseableModule) mod).injectDisposeListeners(registry.getDisposeListeners());
            }
        }

        CloseableInjectorImpl impl = new CloseableInjectorImpl(Guice.createInjector(stage, m), registry);
        registry.setInjector(impl);

        return impl;
    }

    @Override
    public <T> T getInstance(Key<T> key) {
        registry.getProvisionInstances().clear();

        return afterInjection(super.getBinding(key));
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        registry.getProvisionInstances().clear();

        return afterInjection(super.getBinding(type));
    }

    private <T> T afterInjection(Binding<T> binding) {
        T instance = binding.getProvider().get();

        if (registry.getProvisionInstances().size() != 1) {
            for (Key<?> k : registry.getProvisionInstances().keySet()) {
                System.out.println(" - Provisioned but not bound: " + k);
            }

            if(registry.getProvisionInstances().isEmpty() && !Scopes.isSingleton(binding))
                throw new IllegalStateException("Expected exactly one provisioned instance, but got " +
                    registry.getProvisionInstances().size() +
                    ". This means the CloseableInjector did not detect the dependencies correctly." +
                    " Some fields/constructor variables where not linked to provisioned instances.");
        }
        registry.getProvisionInstances().clear();

        return instance;
    }

    @Override
    public <T> void close(T instance) {
        registry.getDependencyGraph().close(instance);
    }

    @Override
    public void close() {
        registry.getDependencyGraph().close();
    }

    public String debugGraph() {
        return registry.getDependencyGraph().debug();
    }

}
