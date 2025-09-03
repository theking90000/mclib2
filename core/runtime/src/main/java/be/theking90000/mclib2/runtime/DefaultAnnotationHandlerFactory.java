package be.theking90000.mclib2.runtime;

/**
 * The default factory which simply uses the no-arg constructor to instantiate the handler.
 */
public class DefaultAnnotationHandlerFactory extends AbstractAnnotationHandlerFactory {

    @Override
    public <T extends AnnotationHandler<V>, V> T create(Class<T> loaderClass) throws Exception {
        return loaderClass.getDeclaredConstructor().newInstance();
    }

    @Override
    public <T extends AnnotationHandler<V>, V> void destroy(T handler) throws Exception {
        handler.destroy();
    }

    @Override
    protected boolean isFactory(Class<? extends AnnotationHandlerFactory> clazz) {
        return clazz == null || clazz.isAssignableFrom(this.getClass());
    }
}
