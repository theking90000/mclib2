package be.theking90000.mclib2.platform.boot;

import be.theking90000.mclib2.platform.*;
import be.theking90000.mclib2.platform.classpath.PlatformClasspath;

import java.lang.reflect.Constructor;
import java.util.*;

public class PlatformRegistry {

    private final Map<PluginDescriptor, RegisteredPlugin<?>> registeredPlugins = new HashMap<>();
    private final Set<RegisteredPlugin<?>> bootedPlugins = new HashSet<>();
    private PlatformClasspath platformClasspath;

    public PlatformRegistry() {
        platformClasspath = new PlatformClasspath();
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
     * @param descriptor        the plugin descriptor
     * @param <T>               type of the custom data
     * @param customData        optional custom data passed by the platform adapter (e.g. Bukkit's JavaPlugin instance)
     * @param callerClassLoader the classloader of the caller
     * @return the number of dependencies loaded (excluding already loaded ones)
     */
    public <T> int register(PluginDescriptor descriptor, T customData, ClassLoader callerClassLoader) {
        RegisteredPlugin<T> r = new RegisteredPlugin<>(descriptor, customData, platformClasspath.createPluginLoader(), callerClassLoader);
        this.registeredPlugins.put(descriptor, r);

        this.platformClasspath.registerDependencies(r);

        if (descriptor.loadImmediately) {
            return bootPlugin(r);
        }

        return 0;
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
    public int unregister(PluginDescriptor descriptor) {
        RegisteredPlugin<?> r = this.registeredPlugins.get(descriptor);
        if (r != null && this.bootedPlugins.contains(r)) {
            return this.shutdownPlugin(r);
        }
        return 0;
    }

    /**
     * Informs the platform boot system that all plugins have been registered.
     * This allows it to finalize loading, such as resolving shared dependencies and booting plugins.
     *
     * @return the total number of dependencies loaded during this finalization (excluding already loaded ones).
     */
    public int boot() {
        int loaded = 0;
        for (RegisteredPlugin<?> r : registeredPlugins.values()) {
            if (!bootedPlugins.contains(r)) {
                loaded += bootPlugin(r);
            }
        }

        return loaded;
    }

    /**
     * Shuts down all registered plugins and closes all resources.
     *
     * @return the total number of dependencies unloaded (if any, usually 0)
     */
    public int shutdown() {
        int unloaded = 0;
        for (RegisteredPlugin<?> r : registeredPlugins.values()) {
            if (bootedPlugins.contains(r)) {
                unloaded += shutdownPlugin(r);
            }
        }
        this.platformClasspath.shutdown();
        this.registeredPlugins.clear();
        this.bootedPlugins.clear();
        platformClasspath = new PlatformClasspath();
        return unloaded;
    }

    private int bootPlugin(RegisteredPlugin<?> r) {
        int l = this.platformClasspath.loadPluginDependencies(r);

        this.bootedPlugins.add(r);

        this.callEntrypoints(r);

        return l;
    }

    private int shutdownPlugin(RegisteredPlugin<?> r) {
        int unloaded = 0;

        this.platformClasspath.unregisterDependencies(r);

        return unloaded;
    }


    private void callEntrypoints(RegisteredPlugin<?> r) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(r.classLoader);
        PriorityQueue<Constructor<?>> entrypoints = new PriorityQueue<>((a, b) -> {
            EntrypointPriority pa = a.getAnnotation(EntrypointPriority.class);
            EntrypointPriority pb = b.getAnnotation(EntrypointPriority.class);

            int va = pa == null ? Priority.NORMAL.ordinal() : pa.value().ordinal();
            int vb = pb == null ? Priority.NORMAL.ordinal() : pb.value().ordinal();

            return Integer.compare(vb, va);
        });
        for (String entrypoint : r.descriptor.entryPoints) {
            try {
                Class<?> cls = Class.forName(entrypoint, true, r.classLoader);

                try {
                    for (Constructor<?> c : cls.getDeclaredConstructors()) {
                        if (c.isAnnotationPresent(PlatformEntrypoint.class)) {
                            entrypoints.add(c);
                        }
                    }
                } catch (Throwable exp) {
                    if (exp instanceof NoClassDefFoundError) {
                        continue;
                    }
                    throw exp;
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load entrypoint " + entrypoint, e);
            }
        }

        PlatformStore.enter();

        while (!entrypoints.isEmpty()) {
            Constructor<?> c = entrypoints.poll();
            try {
                if (c.getParameterCount() == 0) {
                    c.setAccessible(true);
                    c.newInstance();
                } else if (c.getParameterCount() == 1 && r.customData != null && c.getParameterTypes()[0].isAssignableFrom(r.customData.getClass())) {
                    c.setAccessible(true);
                    c.newInstance(r.customData);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to call entrypoint " + c.getDeclaringClass().getName(), e);
            }
        }

        PlatformStore.exit();
        Thread.currentThread().setContextClassLoader(cl);
    }


}
