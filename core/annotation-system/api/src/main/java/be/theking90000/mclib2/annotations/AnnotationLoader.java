package be.theking90000.mclib2.annotations;

import java.lang.annotation.*;

/**
 * Meta-annotation that marks a class as the "loader" (or handler)
 * for a specific {@link RegisteredAnnotation}.
 *
 * <p>How it works:</p>
 * <ul>
 *   <li>You create a class that implements {@link AnnotationHandler}.</li>
 *   <li>You annotate it with {@code @AnnotationLoader(MyAnnotation.class)}.</li>
 *   <li>The annotation processor will generate metadata that links
 *       {@code MyAnnotation} to your loader class.</li>
 *   <li>At runtime, the {@code ModuleSystem} will instantiate your loader
 *       and call it for every class annotated with {@code MyAnnotation}.</li>
 * </ul>
 *
 * <p>This allows you to plug in new behaviors into the system simply by
 * defining a new annotation + loader pair.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * {@literal @}AnnotationLoader(Service.class)
 * public class ServiceLoader implements AnnotationHandler<Service> {
 *     public void handle(Class<Service> clazz) {
 *         // Register clazz as a service in the ServiceRegistry
 *    }
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface AnnotationLoader {
    /**
     * The annotation type that this loader is responsible for.
     * @return the annotation class this loader handles
     */
    Class<? extends Annotation> value();
}