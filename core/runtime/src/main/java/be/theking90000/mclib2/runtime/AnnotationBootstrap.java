package be.theking90000.mclib2.runtime;

import be.theking90000.mclib2.annotations.InjectStrategy;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * High-level entrypoint for the annotation-based runtime.
 *
 * <p>This class ties together:
 * <ul>
 *   <li>{@link AnnotationDiscovery} – finds handlers and annotated classes
 *       from compile-time generated metadata.</li>
 *   <li>{@link AnnotationHandlerFactory} – instantiates handlers using
 *       the appropriate strategy (default, Guice, Spring, etc.).</li>
 *   <li>{@link AnnotationHandler} – processes the discovered annotated classes.</li>
 * </ul>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * // With default factory
 * AnnotationBootstrap bootstrap = new AnnotationBootstrap(new DefaultAnnotationHandlerFactory());
 * bootstrap.bootstrap();
 *
 * // With Guice
 * Injector injector = Guice.createInjector(new MyAppModule());
 * AnnotationBootstrap bootstrap = new AnnotationBootstrap(new GuiceAnnotationHandlerFactory(injector));
 * bootstrap.bootstrap();
 * }</pre>
 */
public class AnnotationBootstrap {
    private final AnnotationHandlerFactory factory;

    public AnnotationBootstrap(AnnotationHandlerFactory factory) {
        this.factory = factory;
    }

    /**
     * Bootstraps the system:
     * <ol>
     *   <li>Discovers all annotation handlers and annotated classes.</li>
     *   <li>Instantiates handlers via the provided factory.</li>
     *   <li>Dispatches annotated classes to the appropriate handler.</li>
     * </ol>
     *
     * @param result the result of annotation discovery
     */
    public void bootstrap(AnnotationDiscovery.AnnotationResult result) {
        for (Class<? extends Annotation> annotation : result.getDiscoveredAnnotations()) {
            Set<Class<? extends AnnotationHandler<?>>> handlerClasses = result.getHandlers(annotation);
            Set<Class<?>> annotatedClasses = result.getAnnotatedClasses(annotation);

            for (Class<? extends AnnotationHandler<?>> handlerClass : handlerClasses) {
                try {
                    if (factory.supports(handlerClass)) {
                        createHandlerClass(handlerClass, annotatedClasses);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to bootstrap handler: " + handlerClass, e);
                }
            }
        }
    }

    private <T extends AnnotationHandler<V>, V> void createHandlerClass(Class<T> handlerClass, Set<Class<?>> annotatedClasses) throws Exception {
        AnnotationHandler<V> handler = factory.create(handlerClass);

        for (Class<?> annotatedClass : annotatedClasses) {
            handler.handle((Class<V>) annotatedClass);
        }
    }


}