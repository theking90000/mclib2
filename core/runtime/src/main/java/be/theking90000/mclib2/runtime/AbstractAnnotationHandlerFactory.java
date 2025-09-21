package be.theking90000.mclib2.runtime;

import be.theking90000.mclib2.annotations.InjectStrategy;

/**
 * An abstract implementation of an AnnotationHandlerFactory.
 * This class provides a default implementation for the {@link #supports(Class)} method,
 * which checks if the factory can handle a given AnnotationHandler class based on its
 * {@link InjectStrategy} annotation.
 *
 * <p>Subclasses should implement the actual instantiation logic for the
 * AnnotationHandlers they support.</p>
 */
public abstract class AbstractAnnotationHandlerFactory implements AnnotationHandlerFactory {

    @Override
    public <T extends AnnotationHandler<V>, V> boolean supports(Class<T> loaderClass) {
        Class<? extends AnnotationHandlerFactory> factoryClass = getInjectStrategy(loaderClass);

        return isFactory(factoryClass);
    }

    /**
     * Checks if the given factory class is compatible with this factory.
     *
     * @param clazz the factory class to check
     * @return true if this factory can instantiate handlers requiring the given factory, false otherwise
     */
    protected boolean isFactory(Class<? extends AnnotationHandlerFactory> clazz) {
        // if (clazz == null) return false;
        return clazz == this.getClass();
    }

    private Class<? extends AnnotationHandlerFactory> getInjectStrategy(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(InjectStrategy.class))
            return null;

        InjectStrategy strategy = clazz.getAnnotation(InjectStrategy.class);

        return strategy.value();
    }
}
