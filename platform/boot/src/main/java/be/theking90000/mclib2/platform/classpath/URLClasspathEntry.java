package be.theking90000.mclib2.platform.classpath;

import java.net.URL;
import java.util.Objects;

public class URLClasspathEntry extends ClasspathEntry {

    private final URL url;

    public URLClasspathEntry(String globalID, String uniqueID, URL url) {
        super(globalID, uniqueID);
        this.url = url;
    }

    @Override
    public URL resolve(ClassLoader requestedBy) throws Exception {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        URLClasspathEntry that = (URLClasspathEntry) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), url);
    }
}
