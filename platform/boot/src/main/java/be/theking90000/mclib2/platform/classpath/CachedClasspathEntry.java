package be.theking90000.mclib2.platform.classpath;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class CachedClasspathEntry extends ClasspathEntry {

    private static final String CACHE_DIR = ".mclib2/cache";

    static {
        try {
            Files.createDirectories(Paths.get(CACHE_DIR));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create cache directory: " + CACHE_DIR, e);
        }
    }

    private final ClasspathEntry delegate;
    private final String sha256;

    public CachedClasspathEntry(ClasspathEntry delegate, String sha256) {
        super(delegate.getGlobalID(), delegate.getUniqueID());
        this.delegate = delegate;
        this.sha256 = sha256;
    }

    public Path cachedFile() {
        return Paths.get(CACHE_DIR, getGlobalID()+"-"+getUniqueID()+".jar");
    }

    @Override
    public URL resolve(ClassLoader requestBy) throws Exception {
        if(check()) {
            return cachedFile().toUri().toURL();
        }

        URL url = delegate.resolve(requestBy);

        try (InputStream in = url.openStream()) {
            Files.copy(in, cachedFile(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return cachedFile().toUri().toURL();
        }
    }

    private boolean check() {
        Path cached = cachedFile();
        if (!Files.exists(cached)) {
            return false;
        }
        try (InputStream in = Files.newInputStream(cached)) {
            String fileHash = ClasspathEntry.sha256(in);
            return fileHash.equalsIgnoreCase(sha256);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CachedClasspathEntry that = (CachedClasspathEntry) o;
        return Objects.equals(delegate, that.delegate) && Objects.equals(sha256, that.sha256);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), delegate, sha256);
    }
}
