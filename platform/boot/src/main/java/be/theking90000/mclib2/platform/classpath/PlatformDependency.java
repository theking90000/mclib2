package be.theking90000.mclib2.platform.classpath;

import java.net.URL;
import java.util.Objects;

public class PlatformDependency {

    private ClasspathEntry entry;
    private ClassLoader requestedBy;
    private boolean loaded = false;

    private UnpackedDependency unpacked = null;

    public PlatformDependency(ClasspathEntry entry, ClassLoader requestedBy) {
        this.entry = entry;
        this.requestedBy = requestedBy;
    }

    public ClasspathEntry getClasspathEntry() {
        return entry;
    }

    protected URL resolve() {
        try {
            return entry.resolve(requestedBy);
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve dependency: " + entry, e);
        }
    }

    protected UnpackedDependency unpack() {
        if (unpacked != null) return unpacked;
        try {
            return (unpacked = new UnpackedDependency(resolve()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to unpack dependency: " + entry, e);
        }
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
