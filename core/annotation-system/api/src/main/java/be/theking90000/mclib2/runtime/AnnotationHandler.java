package be.theking90000.mclib2.runtime;

import be.theking90000.mclib2.annotations.AnnotationLoader;
import be.theking90000.mclib2.annotations.RegisteredAnnotation;

/**
 * SPI (Service Provider Interface) for handling classes annotated
 * with a specific {@link RegisteredAnnotation}.
 *
 * <p>Every loader must implement this interface. The system will:</p>
 * <ol>
 *   <li>Discover the loader class via {@link AnnotationLoader} metadata.</li>
 *   <li>Instantiate it at runtime.</li>
 *   <li>Call {@link #handle(Class)} for each class annotated with the
 *       corresponding annotation.</li>
 * </ol>
 *
 * <p>This is where you implement the actual runtime behavior of your
 * annotation. For example, a {@code ServiceLoader} might register
 * the class into a DI container, while a {@code ListenerLoader}
 * might register it into an event bus.</p>
 *
 * <p>
 * <b>Type Parameter {@code T}:</b> The base type or interface that annotated classes must extend or implement.
 * This allows handlers to restrict processing to a specific type hierarchy.
 * </p>
 */
public interface AnnotationHandler<T> {
    /**
     * Called for each class annotated with the supported annotation.
     *
     * @param clazz the annotated class, which extends or implements {@code T}
     * @throws Exception if the handler cannot process the class
     */
    void handle(Class<? extends T> clazz) throws Exception;
}
