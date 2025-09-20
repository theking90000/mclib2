package be.theking90000.mclib2.platform.classpath;

import java.util.Objects;

public class PlatformDependency {

    private ClasspathEntry entry;
    private ClassLoader requestedBy;
    private boolean loaded = false;

    public PlatformDependency(ClasspathEntry entry, ClassLoader requestedBy) {
        this.entry = entry;
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
            classpath.appendUrlToClasspath(entry.resolve(requestedBy));
            if (!loaded) {
                loaded = true;
                return false;
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load dependency: " + entry, e);
        }
    }

    public ClasspathEntry getClasspathEntry() {
        return entry;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PlatformDependency that = (PlatformDependency) o;
        return Objects.equals(entry, that.entry);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entry);
    }
}
