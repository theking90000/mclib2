package be.theking90000.mclib2.platform.classpath;

import be.theking90000.mclib2.platform.PluginDescriptor;

import java.util.Objects;

public class PlatformDependency {

    private PluginDescriptor.Dependency descriptor;
    private ClassLoader requestedBy;
    private PlatformDependencyLoader loader = null;

    public PlatformDependency(PluginDescriptor.Dependency descriptor, ClassLoader requestedBy) {
        this.descriptor = descriptor;
        this.requestedBy = requestedBy;
    }

    /**
     * Loads the dependency if not already loaded. This method is thread-safe.
     *
     * @param classpath The classpath appender to use.
     * @return true if the dependency was loaded successfully or was already loaded, false otherwise.
     */
    public boolean load(ClasspathAppender classpath) {
        try {
            return getLoader().load(classpath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load dependency: " + descriptor, e);
        }
    }

    private PlatformDependencyLoader getLoader() {
        if (loader == null) {
            loader = new PlatformDependencyLoader(descriptor, requestedBy);
        }
        return loader;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PlatformDependency that = (PlatformDependency) o;
        return Objects.equals(descriptor, that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(descriptor);
    }

    public PluginDescriptor.Dependency getDescriptor() {
        return descriptor;
    }
}
