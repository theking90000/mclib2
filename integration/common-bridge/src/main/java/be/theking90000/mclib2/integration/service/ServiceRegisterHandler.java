package be.theking90000.mclib2.integration.service;

import be.theking90000.mclib2.annotations.AnnotationLoader;
import be.theking90000.mclib2.annotations.InjectStrategy;
import be.theking90000.mclib2.integration.Service;
import be.theking90000.mclib2.integration.guice.GuiceModuleAnnotationHandlerFactory;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.ScopeAnnotation;

import java.lang.annotation.Annotation;
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
        if(checkScopeAnnotation(clazz))
            return;

        serviceClasses.add(clazz);

        modules.add(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(clazz).asEagerSingleton();
            }
        });
    }

    private boolean checkScopeAnnotation(Class<?> cls) {
        for(Annotation a: cls.getAnnotations()) {
            if(a.annotationType().isAnnotationPresent(ScopeAnnotation.class))
                return true;
        }
        return false;
    }

    @Override
    public void destroy() throws Exception {
        serviceClasses.clear();
        System.out.println("Destroying ServiceRegisterHandler");
    }
}
