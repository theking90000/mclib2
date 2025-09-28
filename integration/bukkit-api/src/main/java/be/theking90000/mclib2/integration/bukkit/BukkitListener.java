package be.theking90000.mclib2.integration.bukkit;

import be.theking90000.mclib2.annotations.RegisteredAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class is a Bukkit listener
 * and should be registered as such.
 * <p>
 *     When a class is annotated with this annotation,
 *     it will be automatically registered as a Bukkit listener
 *     when the plugin is enabled.
 *     The class must implement org.bukkit.event.Listener.
 * </p>
 */
@RegisteredAnnotation
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BukkitListener {
}
