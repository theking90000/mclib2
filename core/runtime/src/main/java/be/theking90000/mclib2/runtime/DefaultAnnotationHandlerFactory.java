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
    protected boolean isFactory(Class<? extends AnnotationHandlerFactory> clazz) {
        return clazz == null || clazz.isAssignableFrom(this.getClass());
    }
}
