package be.theking90000.mclib2.integration;

import be.theking90000.mclib2.annotations.AnnotationLoader;
import be.theking90000.mclib2.annotations.InjectStrategy;
import be.theking90000.mclib2.integration.guice.GuiceModuleAnnotationHandlerFactory;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.google.inject.Binder;
import com.google.inject.Module;

import java.util.HashSet;
import java.util.Set;

@AnnotationLoader(Service.class)
@InjectStrategy(GuiceModuleAnnotationHandlerFactory.class)
public class ServiceRegisterHandler implements AnnotationHandler<Object> {

    private final Set<Module> modules;
    private final Set<Class<?>> serviceClasses = new HashSet<>();

    public ServiceRegisterHandler(Set<Module> modules) {
        this.modules = modules;
    }

    @Override
    public void handle(Class<?> clazz) throws Exception {
        serviceClasses.add(clazz);

        modules.add(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(clazz).asEagerSingleton();
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("Destroying ServiceRegisterHandler");
    }
}
