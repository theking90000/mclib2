package be.theking90000.mclib2.platform.gradle.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;

import be.theking90000.mclib2.platform.gradle.plugin.ClassProcessor;

import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public abstract class RunClassProcessorsTask extends DefaultTask {

    @InputFiles
    public abstract ConfigurableFileCollection getCompileClasspath();

    @InputFiles
    public abstract ConfigurableFileCollection getRuntimeClasspath();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void runProcessors() throws Exception {
        // Prepare classloader with input files
        URL[] urls = Stream.concat(getCompileClasspath().getFiles().stream(), getRuntimeClasspath().getFiles().stream())
                .map(f -> {
                    try {
                        return f.toURI().toURL();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .distinct()
                .toArray(java.net.URL[]::new);

        try (URLClassLoader loader = new URLClassLoader(urls, (ClassLoader) Thread.currentThread().getContextClassLoader())) {
            ServiceLoader<ClassProcessor> sl = ServiceLoader.load(ClassProcessor.class, loader);


            Collection<ClassProcessor> processors = new HashSet<>();
            try {
                sl.forEach(processors::add);
            } catch (Throwable t) {
                getLogger().warn("Failed to load class processors", t);
                throw t;
            }

            if (processors.isEmpty())
                return;

            // ServiceLoader.load(ClassProcessor.class, loader)
            //ClassProcessor serviceLoader = ServiceLoader.load(ClassProcessor.class, loader);

            // Discover classes to process
            for (File f : getRuntimeClasspath().getFiles()) {
                getProject().fileTree(f).matching(it -> it.include("**/*.class")).forEach(file -> {
                    String className = stripCommonPrefix(f.toPath(), file.toPath())
                            .toString()
                            .replace(File.separatorChar, '.')
                            .replaceAll("\\.class$", "");

                    // Windows Fix : drive Letter (.*): in classname
                    className = className.substring(className.indexOf(':') + 1);

                    if (className.startsWith(".")) {
                        className = className.substring(1);
                    }

                    try {
                        Class<?> clazz = loader.loadClass(className);

                        for (ClassProcessor proc : processors) {
                            proc.processClass(clazz, getOutputDir().getAsFile().get().toPath());
                        }
                    } catch (Throwable t) {
                        getLogger().warn("Failed to process class: " + className, t);
                    }
                });
            }


        }
    }

    public static Path stripCommonPrefix(Path base, Path target) {
        // normalize to absolute so names line up
        base = base.toAbsolutePath().normalize();
        target = target.toAbsolutePath().normalize();

        Iterator<Path> baseIter = base.iterator();
        Iterator<Path> targetIter = target.iterator();

        // walk until diverge
        while (baseIter.hasNext() && targetIter.hasNext()) {
            Path bPart = baseIter.next();
            Path tPart = targetIter.next();
            if (!bPart.equals(tPart)) {
                break;
            }
        }

        // `targetIter` now points to the "rest" of target after common prefix
        Path relative = target.getRoot(); // could be null for relative paths
        if (relative == null && target.iterator().hasNext()) {
            relative = targetIter.next();
        }

        while (targetIter.hasNext()) {
            if (relative == null) {
                relative = targetIter.next();
            } else {
                relative = relative.resolve(targetIter.next());
            }
        }

        return relative;
    }

}
