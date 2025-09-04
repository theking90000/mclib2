package be.theking90000.mclib2.platform;

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
    public Set<Dependency> dependencies;

    public PluginDescriptor(boolean loadImmediately, Set<String> entryPoints, Set<Dependency> dependencies) {
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

    /**
     * Describes a dependency needed by a plugin.
     * - devPath: Path to the dependency in a development environment (can be null)
     * - mavenCoordinates: Maven coordinates of the dependency (cannot be null)
     * - mavenRepository: Optional custom maven repository URL (can be null, defaults to Maven Central if null)
     * - sha256: Optional SHA-256 checksum for integrity verification (can be null)
     * A Dependency is considered equal if all its fields are equal.
     * <p>
     * Algorithm to resolve dependencies:
     * 1. If devPath is provided and the file exists, use it.
     * 2. Check if the dependency is extracted in the local cache ($PWD/.mclib2/cache) verifying the SHA-256 if provided.
     * 3. Check if the dependency is included in the plugin jar under /libs and extract it if found.
     * 4. If mavenCoordinates is provided, download it from the specified mavenRepository.
     * 5. If none of the above methods yield the dependency, throw an error indicating the dependency cannot be resolved.
     */
    public static class Dependency implements Serializable {
        private static final long serialVersionUID = 42L;

        public final String devPath; // Path to the dependency in a development environment (can be null)
        public final String mavenCoordinates; // Maven coordinates of the dependency (cannot be null)

        public final String sha256; // Optional SHA-256 checksum for integrity verification (cannot be null)

        public final String mavenRepository; // Optional custom maven repository URL (can be null, defaults to Maven Central if null)

        public Dependency(String devPath, String mavenCoordinates, String sha256, String mavenRepository) {
            this.devPath = devPath;
            this.mavenCoordinates = mavenCoordinates;
            this.sha256 = sha256;
            this.mavenRepository = mavenRepository;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Dependency that = (Dependency) o;
            return Objects.equals(devPath, that.devPath) && Objects.equals(mavenCoordinates, that.mavenCoordinates) && Objects.equals(sha256, that.sha256) && Objects.equals(mavenRepository, that.mavenRepository);
        }

        @Override
        public int hashCode() {
            return Objects.hash(devPath, mavenCoordinates, sha256, mavenRepository);
        }

        public String getStandardFileName() {
            return mavenCoordinates.replace(':', '-').replace('/', '-') + ".jar";
        }

        public String coordinatesWithoutVersion() {
            int lastColon = mavenCoordinates.lastIndexOf(':');
            if (lastColon == -1) return mavenCoordinates;
            return mavenCoordinates.substring(0, lastColon);
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
