package be.theking90000.mclib2.inject;

import com.google.inject.*;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CloseableInjectorImpl extends DelegateInjector implements CloseableInjector {

    private final CloseableRegistry registry;

    protected CloseableInjectorImpl(Injector injector, CloseableRegistry registry) {
        super(injector);

        this.registry = registry;
    }

    public static CloseableInjector createInjector(Stage stage, Collection<Module> modules) {
        CloseableRegistry registry = new CloseableRegistry();

        Iterable<Module> m = Stream.concat(Stream.of(new CloseableModule(registry)), modules.stream()).collect(Collectors.toSet());

        return new CloseableInjectorImpl(Guice.createInjector(stage, m), registry);
    }

    @Override
    public <T> T getInstance(Key<T> key) {
        return afterInjection(super.getInstance(key));
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return afterInjection(super.getInstance(type));
    }

    private <T> T afterInjection(T instance) {
        if (registry.getProvisionInstances().size() != 1) {
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
}
