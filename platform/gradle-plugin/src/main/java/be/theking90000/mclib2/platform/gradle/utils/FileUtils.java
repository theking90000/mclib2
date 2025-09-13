package be.theking90000.mclib2.platform.gradle.utils;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class FileUtils {

    public static InputStream getFileFromJar(File file, String name) throws IOException {
        JarFile jf = new JarFile(file);
        JarEntry entry = jf.getJarEntry(name);
        if (entry != null) {
            InputStream in = jf.getInputStream(entry);
            return new FilterInputStream(in) {
                @Override
                public void close() throws IOException {
                    super.close();
                    jf.close();
                }
            };
        }
        jf.close();
        return null;
    }

    public static List<String> readLines(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        return reader.lines().collect(Collectors.toList());
    }

    public static List<String> readLinesFromJar(File file, String name) throws IOException {
        try (InputStream in = getFileFromJar(file, name)) {
            if (in != null) {
                return readLines(in);
            }
        }
        return Collections.emptyList();
    }

    public static InputStream getFileFromJarOrDirectory(File file, String name) throws IOException {
        if (file.getName().endsWith(".jar")) {
            return getFileFromJar(file, name);
        } else {
            File f = new File(file, name);
            if (f.exists()) {
                return Files.newInputStream(f.toPath());
            }
        }
        return null;
    }

    public static List<String> readLinesFromJarOrDirectory(File file, String name) throws IOException {
        try (InputStream in = getFileFromJarOrDirectory(file, name)) {
            if (in != null) {
                return readLines(in);
            }
        }
        return Collections.emptyList();
    }

    public static String sha256(File file) throws Exception {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return sha256(in);
        }
    }

    public static String sha256(InputStream in) throws Exception {
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

}
