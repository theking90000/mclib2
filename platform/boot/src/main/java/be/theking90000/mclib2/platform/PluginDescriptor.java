package be.theking90000.mclib2.platform;

import be.theking90000.mclib2.platform.classpath.ClasspathEntry;

import java.io.*;
import java.util.Objects;
import java.util.Set;

public class PluginDescriptor implements Serializable {

    private static final long serialVersionUID = 42L;


    /* Whether to load the plugin immediately on startup or to wait until all plugins are loaded,
     * which implies that the PlatformBoot.boot() method will need to be called to load the plugin
     * if not called, the plugin will not be loaded at all. */
    public boolean loadImmediately;

    /* a set of entrypoint classNames which will be called once the classpath for the plugin is ready
     *  each entrypoint must implement PlatformEntrypoint spec. */
    public Set<String> entryPoints;

    /* Dependencies needed to be present at runtime for this plugin to run correctly.
     * PlatformBoot will ensure every dependency is available and that a different version of any dependency will
     * be overridden if needed */
    public Set<ClasspathEntry> dependencies;

    public PluginDescriptor(boolean loadImmediately, Set<String> entryPoints, Set<ClasspathEntry> dependencies) {
        this.loadImmediately = loadImmediately;
        this.entryPoints = entryPoints;
        this.dependencies = dependencies;
    }

    public static PluginDescriptor deserialize(InputStream in) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(in)) {
            return (PluginDescriptor) ois.readObject();
        }
    }

    public void serialize(OutputStream out) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PluginDescriptor that = (PluginDescriptor) o;
        return loadImmediately == that.loadImmediately && Objects.equals(entryPoints, that.entryPoints) && Objects.equals(dependencies, that.dependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loadImmediately, entryPoints, dependencies);
    }
}
