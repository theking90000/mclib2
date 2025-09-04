package be.theking90000.mclib2.platform.boot;

import be.theking90000.mclib2.platform.PluginDescriptor;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * This class stores a static reference to the platform registry.
 */
public class PlatformSingleton {

    private static PlatformRegistry registry;

    private static Class<PlatformSingleton> getPlatformSingleton() {
        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            return (Class<PlatformSingleton>) cl.loadClass(PlatformSingleton.class.getCanonicalName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get PlatformSingleton instance", e);
        }
    }

    // We are forced to use reflection here to avoid classloader issues.
    // Because class A is loaded by classloader 1 and class B is loaded by classloader 2,
    // It will throw an error cannot cast A to B, even if A and B are the same class.
    // Example:
    // This class is loaded by the bukkit plugin classloader
    // hence PlatformRegistry is loaded by the bukkit plugin classloader
    // but the Registry that will load via getPlatformSingleton() is from system classloader
    // if we try to cast PlatformRegistry from the system classloader to the one from the bukkit plugin classloader
    // it will throw an error. (and a quite confusing one because it says cannot cast A to A)
    public static Object getRegistry() {
        Class<PlatformSingleton> clazz = getPlatformSingleton();
        try {
            Method m = clazz.getDeclaredMethod("getRegistry0");
            m.setAccessible(true);
            return m.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get PlatformRegistry instance", e);
        }
    }

    private static Object getRegistry0() {
        if (registry == null) {
            registry = new PlatformRegistry();
        }
        return registry;
    }

    /* Alias to "getRegistry().register(...)" via reflection. */
    public static <T> int register(PluginDescriptor descriptor, T customData, ClassLoader callerClassLoader) {
        try {
            Object registry = getRegistry();
            Method m = registry.getClass().getDeclaredMethod("register", PluginDescriptor.class, Object.class, ClassLoader.class);
            return (int) m.invoke(registry, descriptor, customData, callerClassLoader);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register plugin: " + descriptor, e);
        }
    }

    /* Alias to "getRegistry().boot()" via reflection. */
    public static int boot() {
        try {
            Object registry = getRegistry();
            Method m = registry.getClass().getDeclaredMethod("boot");
            return (int) m.invoke(registry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to boot", e);
        }
    }

    /* Alias to "getRegistry().register(...)" via reflection. */
    public static <T> int unregister(PluginDescriptor descriptor) {
        try {
            Object registry = getRegistry();
            Method m = registry.getClass().getDeclaredMethod("unregister", PluginDescriptor.class);
            return (int) m.invoke(registry, descriptor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to unregister plugin: " + descriptor, e);
        }
    }

    /* Alias to "getRegistry().shutdown(...)" via reflection. */
    public static <T> int shutdown() {
        try {
            Object registry = getRegistry();
            Method m = registry.getClass().getDeclaredMethod("shutdown");
            return (int) m.invoke(registry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to shutdown", e);
        }
    }

}
