package be.theking90000.mclib2.platform;

import be.theking90000.mclib2.platform.classpath.ClasspathEntry;
import be.theking90000.mclib2.platform.classpath.DependencyClassLoader;

import java.util.Objects;
import java.util.Set;

public class RegisteredPlugin<T> {
    public final PluginDescriptor descriptor;
    public final T customData;

    /**
     * The class loader associated with this plugin by mclib2 platform-boot
     */
    public final DependencyClassLoader classLoader;
    /**
     * The class loader of the caller that registered this plugin (e.g. Bukkit's plugin classloader)
     */
    public final ClassLoader callerClassLoader;

    public RegisteredPlugin(PluginDescriptor descriptor, T customData, DependencyClassLoader classLoader, ClassLoader callerClassLoader) {
        this.descriptor = descriptor;
        this.customData = customData;

        this.classLoader = classLoader;
        this.callerClassLoader = callerClassLoader;
    }

    public Set<ClasspathEntry> getDependencies() {
        return descriptor.dependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RegisteredPlugin<?> that = (RegisteredPlugin<?>) o;
        return Objects.equals(descriptor, that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(descriptor);
    }
}
