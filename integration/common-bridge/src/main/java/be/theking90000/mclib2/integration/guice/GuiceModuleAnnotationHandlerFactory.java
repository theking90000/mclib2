package be.theking90000.mclib2.integration.guice;

import be.theking90000.mclib2.runtime.AbstractAnnotationHandlerFactory;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.google.inject.Module;

import java.util.Set;

public class GuiceModuleAnnotationHandlerFactory extends AbstractAnnotationHandlerFactory {

    private final Set<Module> modules;

    public GuiceModuleAnnotationHandlerFactory(Set<Module> modules) {
        this.modules = modules;
    }

    @Override
    public <T extends AnnotationHandler<V>, V> T create(Class<T> loaderClass) throws Exception {
        return loaderClass.getConstructor(Set.class).newInstance(modules);
    }

    @Override
    public <T extends AnnotationHandler<V>, V> void destroy(T handler) throws Exception {
        handler.destroy();
    }

}
