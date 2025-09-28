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

                // noinspection
                binder.bind(clazz).toProvider((jakarta.inject.Provider) p);
            }
        });
    }
}
