package be.theking90000.mclib2.platform.gradle.tasks;

import be.theking90000.mclib2.platform.PluginDescriptor;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

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
        PluginDescriptor descriptor = new PluginDescriptor(/* loadImmediately*/ false, new HashSet<>(), getDependencies());

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

    private Set<PluginDescriptor.Dependency> getDependencies() throws Exception {
        Set<PluginDescriptor.Dependency> dependencies = new HashSet<>();

        if (isDev()) {
            for (File file : getRuntimeClasspath().getFiles()) {
                dependencies.add(
                        new PluginDescriptor.Dependency(
                                file.getAbsolutePath(),
                                "dev:" + file.getAbsolutePath()
                                        .replace("/", "-")
                                        .replace("\\", "-")
                                        .replace(":", "-") + ":0",
                                null,
                                null
                        ));
            }
        } else {
            for (ResolvedArtifact artifact : configuration()
                    .getResolvedConfiguration()
                    .getResolvedArtifacts()) {
                String coordinates = artifact.getModuleVersion().getId().toString();
                File jarFile = artifact.getFile();
                String sha256 = sha256(jarFile);


                dependencies.add(
                        new PluginDescriptor.Dependency(
                                null,
                                coordinates,
                                sha256,
                                null
                        ));
            }
        }

        if (!isDev()) {
            dependencies.add(new PluginDescriptor.Dependency(
                    null, getProject().getGroup() + ":" +
                    getProject().getName() + ":" +
                    getProject().getVersion(),
                    sha256(getCodeFile().get().getAsFile()),
                    null));
        }

        return dependencies;
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
