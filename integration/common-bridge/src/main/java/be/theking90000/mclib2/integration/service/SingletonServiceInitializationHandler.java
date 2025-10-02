package be.theking90000.mclib2.integration.service;

import be.theking90000.mclib2.annotations.AnnotationLoader;
import be.theking90000.mclib2.annotations.InjectStrategy;
import be.theking90000.mclib2.inject.CloseableInjector;
import be.theking90000.mclib2.integration.Service;
import be.theking90000.mclib2.integration.guice.GuiceInjectorAnnotationHandlerFactory;
import be.theking90000.mclib2.integration.scope.ScopeCreationListener;
import be.theking90000.mclib2.integration.scope.ScopeDeletionListener;
import be.theking90000.mclib2.integration.scope.ScopeManager;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.google.inject.Inject;
import com.google.inject.Scope;
import com.google.inject.Scopes;

import java.util.HashSet;
import java.util.Set;

@AnnotationLoader(Service.class)
@InjectStrategy(GuiceInjectorAnnotationHandlerFactory.class)
public class SingletonServiceInitializationHandler implements AnnotationHandler<Service>,
        ScopeCreationListener, ScopeDeletionListener {

    private final ScopeManager scopeManager;
    private final CloseableInjector injector;

    private final Set<Class<?>> serviceClasses = new HashSet<>();
    private final Set<Object> services = new HashSet<>();

    @Inject
    public SingletonServiceInitializationHandler(ScopeManager scopeManager, CloseableInjector injector) {
        this.scopeManager = scopeManager;
        this.injector = injector;

        this.scopeManager.addListener((ScopeCreationListener) this);
        this.scopeManager.addListener((ScopeDeletionListener) this);
    }

    @Override
    public void handle(Class<? extends Service> clazz) throws Exception {
        // This checks if another scope is not taking care of the service.
        boolean isSingleton = Scopes.isSingleton(injector.getBinding(clazz));

        if (isSingleton) {
            serviceClasses.add(clazz);
        }
    }

    @Override
    public void destroy() throws Exception {
        serviceClasses.clear();
    }

    @Override
    public void onScopeCreation(Scope scope) {
        if(Scopes.SINGLETON == scope) {
            for (Class<?> serviceClass : serviceClasses) {
                services.add(injector.getInstance(serviceClass));
            }
        }
    }

    @Override
    public void onScopeDeletion(Scope scope) {
        if (Scopes.SINGLETON == scope) {
            for (Object service : services) {
                injector.close(service);
            }
            services.clear();
        }
    }
}
