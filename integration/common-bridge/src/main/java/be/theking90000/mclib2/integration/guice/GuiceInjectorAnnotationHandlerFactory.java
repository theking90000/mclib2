package be.theking90000.mclib2.integration.guice;

import be.theking90000.mclib2.inject.CloseableInjector;
import be.theking90000.mclib2.runtime.AbstractAnnotationHandlerFactory;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import be.theking90000.mclib2.runtime.AnnotationHandlerFactory;

public class GuiceInjectorAnnotationHandlerFactory extends AbstractAnnotationHandlerFactory {

    private final CloseableInjector injector;

    public GuiceInjectorAnnotationHandlerFactory(CloseableInjector injector) {
        this.injector = injector;
    }

    @Override
    protected boolean isFactory(Class<? extends AnnotationHandlerFactory> clazz) {
        // Allow non @InjectStrategy annotated classes to be handled by this factory
        return clazz == null || clazz.isAssignableFrom(this.getClass());
    }

    @Override
    public <T extends AnnotationHandler<V>, V> T create(Class<T> loaderClass) throws Exception {
        return injector.getInstance(loaderClass);
    }

    @Override
    public <T extends AnnotationHandler<V>, V> void destroy(T handler) throws Exception {
        handler.destroy();
        injector.close(handler);
    }
}
