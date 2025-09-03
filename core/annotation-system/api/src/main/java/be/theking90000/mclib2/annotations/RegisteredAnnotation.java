package be.theking90000.mclib2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that marks an annotation as "discoverable" by the
 * {@code mclib2} annotation processing system.
 *
 * <p>How it works:</p>
 * <ul>
 *   <li>You define your own annotation (e.g. {@code @Service}, {@code @Listener})
 *       and mark it with {@code @RegisteredAnnotation}.</li>
 *   <li>The annotation processor (in {@code annotations-processor}) will detect
 *       that your annotation is "registered" and will generate metadata that
 *       links classes annotated with it to a runtime handler.</li>
 *   <li>At runtime, the {@code ModuleSystem} can then discover all classes
 *       annotated with your annotation without scanning the entire classpath,
 *       because the metadata was pre-generated at compile time.</li>
 * </ul>
 *
 * <p>This is the cornerstone of the system: it allows you to declare new
 * "semantic annotations" that the runtime can understand, without hardcoding
 * them into the core.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * {@literal @}RegisteredAnnotation
 * {@literal @}Retention(RetentionPolicy.RUNTIME)
 * {@literal @}Target(ElementType.TYPE)
 * public @interface Service {}
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME) // compile-time only
@Target(ElementType.ANNOTATION_TYPE)
public @interface RegisteredAnnotation {
}
