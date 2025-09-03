package be.theking90000.mclib2.runtime;


import be.theking90000.mclib2.annotations.RegisteredAnnotation;

/**
 * Factory interface responsible for creating instances of {@link AnnotationHandler}s.
 *
 * <h2>Context</h2>
 * In the {@code mclib2} modular system, classes annotated with
 * {@link RegisteredAnnotation} (e.g. {@code @Service}, {@code @Listener})
 * are discovered at <em>compile-time</em> by the annotation processor.
 * At <em>runtime</em>, these classes are handed off to an
 * {@link AnnotationHandler}, which knows how to process them
 * (e.g. register them in a DI container, event bus, or service registry).
 *
 * <p>However, different handlers may require different instantiation
 * strategies:</p>
 * <ul>
 *   <li>Some handlers are simple and can be created with a no-arg constructor.</li>
 *   <li>Some handlers require dependency injection (e.g. Guice, Spring),
 *       and must be created by a DI container.</li>
 *   <li>Others might require custom initialization logic.</li>
 * </ul>
 *
 * <p>The {@code AnnotationHandlerFactory} abstraction allows the
 * ModuleSystem to remain agnostic of
 * <em>how</em> handlers are created. Instead, it delegates instantiation
 * to a factory implementation.</p>
 *
 * <h2>Typical Implementations</h2>
 * <ul>
 *   <li>{@code DefaultAnnotationHandlerFactory} –
 *       uses reflection to call a no-arg constructor.</li>
 *   <li>{@code GuiceAnnotationHandlerFactory} –
 *       delegates creation to a Guice {@code Injector}.</li>
 *   <li>{@code SpringAnnotationHandlerFactory} –
 *       delegates creation to a Spring {@code ApplicationContext}.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Example: default factory
 * AnnotationHandlerFactory factory = new DefaultAnnotationHandlerFactory();
 * ServiceLoader loader = factory.create(ServiceLoader.class);
 *
 * // Example: Guice factory
 * Injector injector = Guice.createInjector(new MyAppModule());
 * AnnotationHandlerFactory guiceFactory = new GuiceAnnotationHandlerFactory(injector);
 * GuiceAnnotationLoader loader = guiceFactory.create(GuiceAnnotationLoader.class);
 * }</pre>
 *
 * @see AnnotationHandler
 * @see RegisteredAnnotation
 */
public interface AnnotationHandlerFactory {

    /**
     * Creates a new instance of the given {@link AnnotationHandler} class.
     *
     * <p>The instantiation strategy depends on the factory implementation:
     * it may use reflection, a dependency injection container, or any
     * other mechanism.</p>
     *
     * @param loaderClass the concrete class of the {@link AnnotationHandler}
     *                    to instantiate
     * @param <T>         the type of the handler
     * @param <V>         the type of the annotation value the handler processes
     * @return a fully constructed instance of the handler
     * @throws Exception if the handler cannot be instantiated
     */
    <T extends AnnotationHandler<V>, V> T create(Class<T> loaderClass) throws Exception;

    /**
     * Destroys a previously created handler instance.
     *
     * <p>Implementations may:
     * <ul>
     *   <li>Call {@link AnnotationHandler#destroy()} on the handler.</li>
     *   <li>Unregister it from a DI container.</li>
     *   <li>Release any resources associated with it.</li>
     * </ul>
     *
     * @param handler the handler instance to destroy
     * @param <T>     the handler type
     * @param <V>     the annotation type the handler supports
     * @throws Exception if cleanup fails
     */
    <T extends AnnotationHandler<V>, V> void destroy(T handler) throws Exception;

    /**
     * Determines whether this factory supports instantiating the given
     * {@link AnnotationHandler} class, based on its declared
     * {@link be.theking90000.mclib2.annotations.InjectStrategy}.
     *
     * <p>The {@code InjectStrategy} annotation may specify a factory class
     * or a strategy key. If no annotation is present, the value may be
     * {@code null}, in which case the factory should decide whether it
     * wants to act as the "default" strategy.</p>
     *
     * <p>This method allows multiple factories to coexist: the runtime
     * can query each factory in turn and delegate instantiation to the
     * first one that returns {@code true}.</p>
     *
     * @param loaderClass the handler class to check
     * @param <T>         the type of the handler
     * @param <V>         the type of the annotation value the handler processes
     * @return {@code true} if this factory supports creating instances
     * of the given class, {@code false} otherwise
     */
    <T extends AnnotationHandler<V>, V> boolean supports(Class<T> loaderClass);


}
