package be.theking90000.mclib2.platform.gradle.generator;

import be.theking90000.mclib2.platform.PluginDescriptor;
import be.theking90000.mclib2.platform.classpath.ClasspathEntry;
import be.theking90000.mclib2.platform.gradle.utils.FileUtils;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class PluginDescriptorGenerator {

    private final boolean devMode;
    private final boolean embedded;
    private final boolean loadImmediately;

    public PluginDescriptorGenerator(boolean devMode, boolean embedded, boolean loadImmediately) {
        this.devMode = devMode;
        this.embedded = embedded;
        this.loadImmediately = loadImmediately;
    }

    public PluginDescriptorGenerator(boolean devMode) {
        this(devMode, true, false);
    }

    public PluginDescriptor generate(Set<File> runtimeClasspath, Set<ResolvedArtifact> resolvedArtifacts, CodeFile codeFile) {
        Set<String> entryPoints = collectEntryPoints(runtimeClasspath);
        Set<ClasspathEntry> classpath = Stream.concat(
                toClasspathEntryFromArtifacts(resolvedArtifacts),
                toClasspathEntryFromRuntimeClasspath(runtimeClasspath)
        ).collect(HashSet::new, Set::add, Set::addAll);

        if (!devMode) {
            classpath.add(toClasspathEntry(codeFile));
        }

        return new PluginDescriptor(loadImmediately, entryPoints, classpath);
    }

    private Set<String> collectEntryPoints(Set<File> files) {
        Set<String> entryPoints = new HashSet<>();
        for (File file : files) {
            try {
                entryPoints.addAll(FileUtils.readLinesFromJarOrDirectory(file, "entrypoints.txt"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to read entrypoints from " + file, e);
            }
        }
        return entryPoints;
    }

    private Stream<ClasspathEntry> toClasspathEntryFromArtifacts(Set<ResolvedArtifact> resolvedArtifacts) {
        return resolvedArtifacts.stream()
                .map(this::toClasspathEntry);
    }

    private Stream<ClasspathEntry> toClasspathEntryFromRuntimeClasspath(Set<File> files) {
        return files.stream()
                .map(this::toClasspathEntry)
                .filter(Objects::nonNull);
    }

    private ClasspathEntry toClasspathEntry(ResolvedArtifact artifact) {
        ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
        String coordinates = id.getGroup() + ":" + id.getName();
        String sha256;
        if (!embedded) {
            try {
                sha256 = FileUtils.sha256(artifact.getFile());
            } catch (Exception e) {
                throw new RuntimeException("Failed to compute SHA-256 for " + artifact.getFile(), e);
            }
        }
        String fileName = "libs/" + coordinates.replace(":", ".") + "-" + id.getVersion() + ".jar";

        if (embedded) {
            return ClasspathEntry.embedded(coordinates, id.getVersion(), fileName);
        } else {
            throw new RuntimeException("Only embedded descriptor generator is supported at the moment");
        }
    }

    private ClasspathEntry toClasspathEntry(File file) {
        if (!this.devMode) return null;

        String fileName = file.getAbsolutePath()
                .replace("/", "-")
                .replace("\\", "-")
                .replace(":", "-");

        try {
            return ClasspathEntry.file(fileName, "", file.toPath());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private ClasspathEntry toClasspathEntry(CodeFile file) {
        String fileName = file.group + "-" + file.name + "-" + file.version + ".jar";
        return ClasspathEntry.embedded(
                file.group + ":" + file.name,
                file.version,
                "libs/" + fileName
        );
    }

    public static final class CodeFile {
        public final String group;
        public final String name;
        public final String version;

        public CodeFile(String group, String name, String version) {
            this.group = group;
            this.name = name;
            this.version = version;
        }
    }

}
