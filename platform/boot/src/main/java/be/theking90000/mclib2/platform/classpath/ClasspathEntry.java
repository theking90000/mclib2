package be.theking90000.mclib2.platform.classpath;

import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Objects;

public abstract class ClasspathEntry implements Serializable {
    private static final long serialVersionUID = 42L;

    protected final String uniqueID;
    protected final String globalID;

    public ClasspathEntry(String globalID, String uniqueID) {
        this.uniqueID = uniqueID;
        this.globalID = globalID;
    }

    protected static String sha256(InputStream in) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            md.update(buffer, 0, read);
        }

        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static ClasspathEntry embedded(String globalID, String uniqueID, String fileName) {
        return new EmbeddedClasspathEntry(globalID, uniqueID, fileName);
    }

    public static ClasspathEntry url(String globalID, String uniqueID, URL url) {
        return new URLClasspathEntry(globalID, uniqueID, url);
    }

    public static ClasspathEntry file(String globalID, String uniqueID, Path file) throws MalformedURLException {
        return url(globalID, uniqueID, file.toUri().toURL());
    }

    public static ClasspathEntry cached(ClasspathEntry delegate, String sha256) {
        return new CachedClasspathEntry(delegate, sha256);
    }

    public String getGlobalID() {
        return globalID;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public abstract URL resolve(ClassLoader requestedBy) throws Exception;

    public ClasspathEntry cached(String sha256) {
        return cached(this, sha256);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ClasspathEntry that = (ClasspathEntry) o;
        return Objects.equals(uniqueID, that.uniqueID) && Objects.equals(globalID, that.globalID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueID, globalID);
    }
}
