package be.theking90000.mclib2.platform.gradle.tasks;

import be.theking90000.mclib2.platform.PluginDescriptor;
import be.theking90000.mclib2.platform.classpath.ClasspathEntry;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedModuleVersion;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class GeneratePluginDescriptor extends DefaultTask {

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @Input
    public abstract Property<String> getConfiguration();

    @Input
    @Optional
    public abstract Property<Boolean> getDevMode();

    @InputFile
    @Optional
    public abstract RegularFileProperty getCodeFile();

    @InputFiles
    @Optional
    public abstract ConfigurableFileCollection getRuntimeClasspath();

    @TaskAction
    public void generate() throws Exception {
        PluginDescriptor descriptor = new PluginDescriptor(/* loadImmediately*/ false, getEntrypoints(), getDependencies());

        File file = getOutputFile().get().getAsFile();
        file.getParentFile().mkdirs();
        try (FileOutputStream os = new FileOutputStream(file)) {
            descriptor.serialize(os);
        }
    }

    private Configuration configuration() {
        return getProject().getConfigurations().getByName(getConfiguration().get());
    }

    private boolean isDev() {
        return getDevMode().getOrElse(false);
    }

    private Set<ClasspathEntry> getDependencies() throws Exception {
        Set<ClasspathEntry> dependencies = new HashSet<>();

        if (isDev()) {
            for (File file : getRuntimeClasspath().getFiles()) {
                dependencies.add(
                        ClasspathEntry.file(
                                file.getAbsolutePath()
                                        .replace("/", "-")
                                        .replace("\\", "-")
                                        .replace(":", "-"),
                                "",
                                file.toPath()
                        ));
            }
        } else {
            for (ResolvedArtifact artifact : configuration()
                    .getResolvedConfiguration()
                    .getResolvedArtifacts()) {
                ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
                String coordinates = id.getGroup()+":"+id.getName();
                //String sha256 = sha256(artifact.getFile());
                String fileName = "libs/" + coordinates.replace(":",".")+"-"+id.getVersion()+".jar";

                dependencies.add(
                        ClasspathEntry.embedded(
                                coordinates,
                                id.getVersion(),
                                fileName
                        )/*.cached(sha256)*/
                );


            }
        }

        if (!isDev()) {
           String fileName = getProject().getGroup()+"-"+getProject().getName()+"-"+getProject().getVersion()+".jar";
           dependencies.add(ClasspathEntry.embedded(
                    getProject().getGroup() + ":" + getProject().getName(),
                    getProject().getVersion().toString(),
                    "libs/"+    fileName)
                   );
        }

        return dependencies;
    }

    private Set<String> getEntrypoints() {
        HashSet<String> entrypoints = new HashSet<>();
        for (File file : getRuntimeClasspath().getFiles()) {
            if (file.getName().endsWith(".jar")) {
                try (JarFile jf = new JarFile(file)) {
                    JarEntry je = jf.getJarEntry("entrypoints.txt");
                    if (je != null) {
                        readEntrypoints(jf.getInputStream(je), entrypoints);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to read entrypoints from jar: " + file, e);
                }
            } else {
                File f = new File(file, "entrypoints.txt");
                if (f.exists()) {
                    try (InputStream in = Files.newInputStream(f.toPath())) {
                        readEntrypoints(in, entrypoints);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to read entrypoints from file: " + f, e);
                    }
                }
            }
        }
        return entrypoints;
    }

    private void readEntrypoints(InputStream in, Set<String> entrypoints) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while (reader.ready()) {
            entrypoints.add(reader.readLine());
        }
    }

    private static String sha256(File file) throws Exception {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return sha256(in);
        }
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

}
