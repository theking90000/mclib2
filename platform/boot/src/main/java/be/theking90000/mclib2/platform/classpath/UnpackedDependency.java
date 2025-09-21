package be.theking90000.mclib2.platform.classpath;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class UnpackedDependency {

    private final Map<String, byte[]> classes = new HashMap<>();
    private final Map<String, Set<URL>> resources = new HashMap<>();

    protected UnpackedDependency(URL url) {
        try {
            addResource(url);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load dependency from " + url, e);
        }
    }

    public Map<String, byte[]> getClasses() {
        return classes;
    }

    public Map<String, Set<URL>> getResources() {
        return resources;
    }

    private void addResource(URL url) throws IOException {
        if (url.getProtocol().equals("jar") || (url.getProtocol().equals("file") && url.getPath().endsWith(".jar"))) {
            try (InputStream urlStream = url.openStream();
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(urlStream);
                 JarInputStream jarInputStream = new JarInputStream(bufferedInputStream)) {
                JarEntry jarEntry;

                while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                    String spec;
                    if (url.getProtocol().equals("jar")) {
                        spec = url.getPath();
                    } else {
                        spec = url.getProtocol() + ":" + url.getPath();
                    }
                    URL theURL = new URL(null, "jar:" + spec + "!/" + jarEntry.getName(), new NestedURLStreamHandler());

                    addURLToResource(jarEntry.getName(), theURL);
                    addClassFromInputStream(jarInputStream, jarEntry.getName());
                    if (jarEntry.getName().endsWith(".jar")) {
                        addResource(theURL);
                    }
                }
            }
        } else if (url.getPath().endsWith(".class")) {
            throw new IllegalStateException("Cannot add classes directly");
        } else {
            try {
                addDirectory(new File(url.toURI()), null);
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void addFile(File file, File directory, String relativePath) throws IOException {
        if (file.isDirectory()) {
            addDirectory(file, relativePath);
        } else if (file.getName().endsWith(".jar")) {
            try {
                addResource(file.toURI().toURL());
                // super.addURL(file.toURI().toURL());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else if (directory != null) {
            try {
                String relativeName = relativePath + directory.toURI().relativize(file.toURI()).getPath();
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    addClassFromInputStream(fileInputStream, relativeName);
                    addURLToResource(relativeName, file.toURI().toURL());
                }
            } catch (MalformedURLException | FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    } // End Modified

    private void addDirectory(File directory, String relativePath) throws IOException {
        if (!directory.isDirectory()) {
            addFile(directory, null, relativePath);
            return;
            // throw new IllegalStateException("Not a directory: " + directory);
        }
        File[] files = directory.listFiles();
        if (files == null) {
            throw new IllegalStateException("No files found in " + directory);
        }
        for (File file : files) {
            addFile(file, directory, relativePath == null? "": relativePath + directory.getName() + "/");
        }
    }

    private void addClassFromInputStream(InputStream inputStream, String relativePath) throws IOException {
        if (relativePath.endsWith(".class")) {
            int len;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] b = new byte[2048];

            while ((len = inputStream.read(b)) > 0) {
                out.write(b, 0, len);
            }
            out.close();
            byte[] classBytes = out.toByteArray();
            String className = resourceToClassName(relativePath);

            classes.put(className, classBytes);
        }
    }

    private String resourceToClassName(String slashed) {
        return slashed.substring(0, slashed.lastIndexOf(".class")).replace("/", ".");
    }

    private void addURLToResource(String name, URL url) {
        Set<URL> set;
        if (resources.containsKey(name)) {
            set = resources.get(name);
        } else {
            set = new HashSet<>();
            resources.put(name, set);
        }
        set.add(url);
    }

}
