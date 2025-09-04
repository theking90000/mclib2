package be.theking90000.mclib2.platform.boot;


import be.theking90000.mclib2.platform.PluginDescriptor;

/**
 * Central static entrypoint for the mclib2 platform runtime.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Registers plugins via {@link PluginDescriptor}.</li>
 *   <li>Resolves and loads required libraries (shared or isolated).</li>
 *   <li>Creates and manages {@link ClassLoader}s for each plugin.</li>
 *   <li>Instantiates and calls plugin entrypoints.</li>
 *   <li>Supports shutdown/unregister of plugins (cleanup + classloader close).</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>Typically, this class is invoked by a platform adapter (e.g. Bukkit, Velocity or Standalone)
 * during plugin load:</p>
 *
 * <pre>{@code
 * public final class MyPluginBootstrap extends JavaPlugin {
 *     {@literal @}Override
 *     public void onLoad() {
 *         PlatformBoot.register(MyPluginDescriptor.generated());
 *     }
 *
 *     {@literal @}Override
 *     public void onDisable() {
 *         PlatformBoot.unregister("MyPlugin");
 *     }
 * }
 * }</pre>
 *
 * <p>The {@link PluginDescriptor} is usually generated at build time by the
 * mclib2 Gradle plugin, which knows the entrypoint class and dependencies.</p>
 */
public final class PlatformBoot {

    private PlatformBoot() {
        // static utility class
    }

    /**
     * Registers and boots a plugin described by the given descriptor.
     *
     * <p>This will:</p>
     * <ol>
     *   <li>Resolve the plugin's libraries (shared vs isolated).</li>
     *   <li>Create a {@link ClassLoader} for the plugin.</li>
     *   <li>Load and instantiate the entrypoint class.</li>
     *   <li>Call its lifecycle method (e.g. {@code onEnable()}).</li>
     * </ol>
     *
     * @param descriptor the plugin descriptor
     * @param <T>        type of the custom data
     * @param customData optional custom data passed by the platform adapter (e.g. Bukkit's JavaPlugin instance)
     * @return the number of dependencies loaded (excluding already loaded ones)
     */
    public static <T> int register(PluginDescriptor descriptor, T customData) {
        // PlatformBoot.class is loaded by the platform adapter's classloader;
        // therefore, we pass it as the callerClassLoader
        return PlatformSingleton.register(descriptor, customData, PlatformBoot.class.getClassLoader());
    }

    /**
     * Unregisters and shuts down a plugin by name.
     *
     * <p>This will:</p>
     * <ol>
     *   <li>Call {@code onDisable()} on the plugin entrypoint (if present).</li>
     *   <li>Close the plugin's classloader.</li>
     *   <li>Remove it from the active registry.</li>
     * </ol>
     *
     * @param descriptor the plugin descriptor
     * @return number of dependencies unloaded (if any, usually 0)
     */
    public static int unregister(PluginDescriptor descriptor) {
        return PlatformSingleton.unregister(descriptor);
    }

    /**
     * Informs the platform boot system that all plugins have been registered.
     * This allows it to finalize loading, such as resolving shared dependencies and booting plugins.
     *
     * @return the total number of dependencies loaded during this finalization (excluding already loaded ones).
     */
    public static int boot() {
        return PlatformSingleton.boot();
    }

    /**
     * Shuts down all registered plugins and closes all resources.
     *
     * @return the total number of dependencies unloaded (if any, usually 0)
     */
    public static int shutdown() {
        return PlatformSingleton.shutdown();
    }

}
