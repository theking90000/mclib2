package be.theking90000.mclib2.runtime;

import be.theking90000.mclib2.annotations.InjectStrategy;

public abstract class AbstractAnnotationHandlerFactory implements AnnotationHandlerFactory {

    @Override
    public <T extends AnnotationHandler<V>, V> boolean supports(Class<T> loaderClass) {
        Class<? extends AnnotationHandlerFactory> factoryClass = getInjectStrategy(loaderClass);

        return isFactory(factoryClass);
    }

    protected boolean isFactory(Class<? extends AnnotationHandlerFactory> clazz) {
        if (clazz == null) return true;

        return clazz.isAssignableFrom(this.getClass());
    }

    private Class<? extends AnnotationHandlerFactory> getInjectStrategy(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(InjectStrategy.class))
            return null;

        InjectStrategy strategy = clazz.getAnnotation(InjectStrategy.class);

        return strategy.value();
    }
}
