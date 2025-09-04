package be.theking90000.mclib2.platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a constructor as the entrypoint of a platform plugin.
 * The constructor must be public and take a single argument of type CustomData passed with the register() call.
 * Empty constructors are also supported, in which case no argument will be passed.
 * The constructor will be called when the plugin is loaded.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface PlatformEntrypoint {
}
