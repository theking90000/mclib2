package be.theking90000.mclib2.inject.listener;

import be.theking90000.mclib2.inject.CloseableRegistry;
import com.google.inject.*;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ProvisionListener;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class CloseableProvisionListener implements ProvisionListener {

    private static final Logger logger = Logger.getLogger(CloseableProvisionListener.class.getName());

    private final CloseableRegistry registry;

    @Inject
    public Injector injector;

    public CloseableProvisionListener(CloseableRegistry registry) {
        this.registry = registry;
    }

    // TODO: add caching for collectDependencies results
    // TODO: since Key<?> -> Dependency<?> can be cached inside registry.
    private Set<Dependency<?>> collectDependencies(Key<?> key) {
        Set<Dependency<?>> deps = new HashSet<>();
        try {
            InjectionPoint ip = InjectionPoint.forConstructorOf(key.getTypeLiteral());
            deps.addAll(ip.getDependencies());
        } catch (ConfigurationException e) {
            // No injectable constructor, ignore
        }

        try {
            for (InjectionPoint pp : InjectionPoint.forInstanceMethodsAndFields(key.getTypeLiteral()))
                deps.addAll(pp.getDependencies());
        } catch (ConfigurationException | NullPointerException e) {
            // No injectable methods/fields, ignore
        }

        return deps;
    }

    @Override
    public <T> void onProvision(ProvisionInvocation<T> provision) {
        Map<Key<?>, ArrayDeque<Object>> provisionInstances = registry.getProvisionInstances();


        logger.finest("Provisioning " + provision.getBinding().getKey());

        // T instance = provision.provision();

        T instance = provision.getBinding().acceptScopingVisitor(new BindingScopingVisitor<T>() {
            @Override
            public T visitEagerSingleton() {
                T instance = provision.provision();
                registry.getSingletonInstances().put(provision.getBinding().getKey(), instance);

                return instance;
            }

            @Override
            public T visitScope(Scope scope) {
                if(scope == Scopes.SINGLETON) {
                    T instance = provision.provision();
                    registry.getSingletonInstances().put(provision.getBinding().getKey(), instance);

                    return instance;
                }
                return provision.provision();
            }

            @Override
            public T visitNoScoping() {
                return provision.provision();
            }

            @Override
            public T visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
                return provision.provision();
            }
        });

        registry.getDependencyGraph().register(instance);

        // Collect stack as direct dependencies of the provisioned instance
        for (Dependency<?> dep : collectDependencies(provision.getBinding().getKey())) {
            logger.finest("Binding dependency " + dep.getKey() + " of " + provision.getBinding().getKey());
            ArrayDeque<Object> deq = provisionInstances.get(dep.getKey());
            Object o = null;

            if (deq != null && !deq.isEmpty()) {
                o = deq.pop();

                if (deq.isEmpty())
                    provisionInstances.remove(dep.getKey());
            }

            if (o == null) {
                // Try to find equivalent Key in provision instances
                for (Key<?> key : provisionInstances.keySet()) {
                    if (dep.getKey().getTypeLiteral().getRawType().isAssignableFrom(key.getTypeLiteral().getRawType())) {
                        deq = provisionInstances.get(key);
                        if (deq != null && !deq.isEmpty()) {
                            o = deq.pop();

                            if (deq.isEmpty())
                                provisionInstances.remove(dep.getKey());
                            break;
                        }
                    }
                }
            }

            if (o == null) {
                o = registry.getSingletonInstances().get(dep.getKey());
            }

            if (o == null) {
                // Try to find equivalent Key (supertype) in singleton
                for (Key<?> key : registry.getSingletonInstances().keySet()) {
                    if (dep.getKey().getTypeLiteral().getRawType().isAssignableFrom(key.getTypeLiteral().getRawType())) {
                        o = registry.getSingletonInstances().get(key);
                        break;
                    }
                }

            }

            if (o == null) {
                logger.finest("No provisioned instance found for dependency " + dep.getKey() + " of " + provision.getBinding().getKey());
                // throw new IllegalStateException("No provisioned instance found for dependency " + dep.getKey() + " of " + provision.getBinding().getKey());
            } else {
                registry.getDependencyGraph().addDependency(instance, o);
            }
        }

        //if (!registry.getSingletonInstances().containsValue(instance)) {
            provisionInstances
                    .computeIfAbsent(provision.getBinding().getKey(), (k) -> new ArrayDeque<>())
                    .push(instance);
        //}
    }
}
