package be.theking90000.mclib2.integration.bukkit;

import com.google.inject.ScopeAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class or field is scoped to a player.
 * <p>
 * This means that a new instance will be created for each player :
 *  - when a player joins, an instance is created
 *  - when a player leaves, the instance is destroyed
 *  - when a player is offline, no instance exists
 *  </p>
 * This is useful for classes that hold player-specific data or functionality.
 *  <p>
 * This annotation can be combined with different provided annotation loaders
 * to provide per-player instances of classes.
 * For example:
 * - @BukkitListener
 * - @Service
 * </p>
 *
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface PlayerScoped {
}
