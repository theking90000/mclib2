package be.theking90000.mclib2.annotations;

import be.theking90000.mclib2.runtime.AnnotationHandlerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares how a given AnnotationLoader should be instantiated.
 *
 * <p>By default, if this annotation is not present, the system will use
 * the "default" factory (simple no-arg constructor instantiation).</p>
 *
 * <p>If present, the value specifies which factory should be used.
 * This allows different DI systems (Guice, Spring, Dagger, etc.)
 * to coexist without the bootstrapper knowing about them.</p>
 *
 * <p>If the runtime bootstrapper uses a factory other than the allowed one, the class will not be instantiated.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InjectStrategy {
    /**
     * The factory class allowed instantiating the loader.
     *
     * @return the factory class
     */
    Class<? extends AnnotationHandlerFactory> value();
}