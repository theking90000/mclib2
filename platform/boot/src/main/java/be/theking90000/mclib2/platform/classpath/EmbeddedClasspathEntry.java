package be.theking90000.mclib2.platform.classpath;

import java.net.URL;
import java.util.Objects;

public class EmbeddedClasspathEntry extends ClasspathEntry {

    private final String fileName;

    public EmbeddedClasspathEntry(String globalID, String uniqueID, String fileName) {
        super(globalID, uniqueID);
        this.fileName = fileName;
    }

    @Override
    public URL resolve(ClassLoader requestedBy) throws Exception {
        return requestedBy.getResource(fileName);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EmbeddedClasspathEntry that = (EmbeddedClasspathEntry) o;
        return Objects.equals(fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fileName);
    }
}
