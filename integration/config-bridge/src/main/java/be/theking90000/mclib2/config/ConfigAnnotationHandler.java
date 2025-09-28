package be.theking90000.mclib2.config;


import be.theking90000.mclib2.annotations.AnnotationLoader;
import be.theking90000.mclib2.annotations.InjectStrategy;
import be.theking90000.mclib2.integration.guice.GuiceModuleAnnotationHandlerFactory;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.google.inject.*;

import java.util.Set;

@AnnotationLoader(Config.class)
@InjectStrategy(GuiceModuleAnnotationHandlerFactory.class)
public class ConfigAnnotationHandler implements AnnotationHandler<Object> {

    private final Set<Module> modules;

    public ConfigAnnotationHandler(Set<Module> modules) {
        this.modules = modules;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Class<?> clazz) throws Exception {
        modules.add(new Module() {
            @Override
            public void configure(Binder binder) {
                Provider<?> p = new ConfigDynamicProvider(clazz);
                // Intellij IDEA bug?
                // It says there is two method candidate for provider
                // And says toProvider(Provider<?>) twice except that one is
                // com.google.inject.Provider<T> and the other one is
                // jakarta.inject.Provider<T> but it seems to treat it
                // as the same method??

                // Even though intellij says there's an error here
                // It compiles and works fine

                // noinspection
                binder.bind(clazz).toProvider((Provider) p);
            }
        });
    }
}
