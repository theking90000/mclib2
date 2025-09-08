package be.theking90000.mclib2.platform.classpath;

import be.theking90000.mclib2.platform.PluginDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;

public class PlatformDependencyLoader {

    // Local Cache directory is $PWD/.mclib2/cache
    private static final String CACHE_DIR = ".mclib2/cache";

    static {
        try {
            Files.createDirectories(Paths.get(CACHE_DIR));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create cache directory: " + CACHE_DIR, e);
        }
    }

    private final PluginDescriptor.Dependency descriptor;
    private final ClassLoader requestedBy;

    private Path dependencyPath = null;

    public PlatformDependencyLoader(PluginDescriptor.Dependency descriptor, ClassLoader requestedBy) {
        this.descriptor = descriptor;
        this.requestedBy = requestedBy;
    }

    protected boolean load(ClasspathAppender appender) throws Exception {
        if (dependencyPath == null) {
            dependencyPath = resolve();
        }

        if (dependencyPath == null) {
            // System.err.println("Failed to resolve dependency: " + descriptor);
            return false;
        }

        appender.appendFileToClasspath(dependencyPath);
        return true;
    }

    private Path resolve() throws Exception {
        Path path = null;
        String fileName = descriptor.getStandardFileName();

        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    // 1. Attempt to use devPath if present
                    if (descriptor.devPath != null) {
                        path = Paths.get(descriptor.devPath);
                    }
                    break;
                case 1:
                    // 2. Check local cache
                    path = Paths.get(CACHE_DIR, fileName);
                    if (!Files.exists(path)) {
                        path = null; // Invalidate path
                    }

                    break;
                case 2:
                    // 3. Check /libs in the plugin jar
                    InputStream in = requestedBy.getResourceAsStream("libs/" + fileName);
                    if (in != null) {
                        path = Paths.get(CACHE_DIR, fileName);
                        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
                    }

                    break;
                case 3:
                    // 4. Download from the maven repository
                    path = downloadFromMaven();
                    break;
            }

            if (path != null) {
                // Check if SHA-256 match
                if (descriptor.sha256 != null) {
                    try (InputStream in = Files.newInputStream(path)) {
                        String hash = sha256(in);
                        if (!hash.equalsIgnoreCase(descriptor.sha256)) {
                            System.err.println("SHA-256 mismatch for " + path + ": expected " + descriptor.sha256 + ", got " + hash);
                            path = null; // Invalidate path
                            continue;
                        }
                    }
                }
                break;
            }
        }

        if (path == null) return null;

        return path.toAbsolutePath();
    }


    private static String sha256(InputStream in) throws Exception {
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

    private Path downloadFromMaven() throws IOException {
        if (this.descriptor.mavenRepository == null) return null;

        Path into = Paths.get(CACHE_DIR, descriptor.getStandardFileName());

        try (InputStream in = new URL(this.descriptor.mavenRepository).openStream()) {
            Files.copy(in, into, StandardCopyOption.REPLACE_EXISTING);
        }

        return into;
    }

}
