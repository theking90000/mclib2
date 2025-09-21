package be.theking90000.mclib2.platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method of a class with an annotated @PlatformEntrypoint constructor as
 * the destroy method of the platform plugin (destructor).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PlatformDestroy {
}
