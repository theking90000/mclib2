package be.theking90000.mclib2.platform.gradle;

import be.theking90000.mclib2.platform.gradle.tasks.GeneratePluginDescriptor;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class GradlePlugin implements Plugin<Project> {

    private static final String[] FRAMEWORK_IMPLEMENTATION = new String[]{
            "be.theking90000.mclib2:core-annotation-system-api"
    };

    private static final String[] FRAMEWORK_RUNTIME = new String[]{
            "be.theking90000.mclib2:core-runtime",
    };

    private static final String[] BOOTSTRAP = new String[]{
            "be.theking90000.mclib2:platform-boot",
    };


    @Override
    public void apply(@NotNull Project target) {
        MCLib2Extension ext = target.getExtensions().create("mclib2", MCLib2Extension.class);
        DependencyResolver dRes = new DependencyResolver(target, ext);

        target.getPluginManager().apply("java");

        target.afterEvaluate(p -> {
            if (!ext.getDisableFramework().get()) {
                for (String s : FRAMEWORK_IMPLEMENTATION) {
                    target.getDependencies().add("implementation", dRes.resolve(s));
                }
            }
            for (String s : BOOTSTRAP) {
                // Unsure about it, maybe add only annotation @PlatformEntrypoint as compileOnly?
                target.getDependencies().add("compileOnly", dRes.resolve(s));
            }
        });

        Configuration runtime = createConfiguration(target, "MCLib2Runtime");
        runtime.extendsFrom(target.getConfigurations().getByName("runtimeClasspath"));


        TaskProvider<Jar> codeJar = target.getTasks().register("codeJar", Jar.class, jar -> {
            jar.setGroup("mclib2");
            jar.getArchiveBaseName().set(target.getGroup() + "-" + target.getName());
            SourceSetContainer sourceSets = target.getExtensions().getByType(SourceSetContainer.class);
            jar.from(sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).getOutput());
        });

        TaskProvider<GeneratePluginDescriptor> descriptor = target.getTasks()
                .register("generatePluginDescriptor", GeneratePluginDescriptor.class, task -> {
                    task.setGroup("mclib2");

                    task.dependsOn(codeJar);

                    task.getCodeFile().set(codeJar.flatMap(Jar::getArchiveFile));

                    SourceSetContainer sourceSets = target.getExtensions().getByType(SourceSetContainer.class);
                    SourceSet sourceSetOutput = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

                    task.getRuntimeClasspath().setFrom(sourceSetOutput.getRuntimeClasspath());

                    task.getConfiguration().set(runtime.getName());
                    task.getOutputFile().set(target.getLayout().getBuildDirectory().file("generated/plugin-descriptor.dat"));
                });

        TaskProvider<GeneratePluginDescriptor> descriptorDev = target.getTasks()
                .register("generatePluginDescriptorDev", GeneratePluginDescriptor.class, task -> {
                    task.setGroup("mclib2");

                    task.dependsOn("build");

                    SourceSetContainer sourceSets = target.getExtensions().getByType(SourceSetContainer.class);
                    SourceSet sourceSetOutput = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                    // task.dependsOn(sourceSetOutput);


                    task.getRuntimeClasspath().setFrom(sourceSetOutput.getRuntimeClasspath());
                    task.getConfiguration().set(runtime.getName());
                    task.getOutputFile().set(target.getLayout().getBuildDirectory().file("generated/plugin-descriptor-dev.dat"));
                    task.getDevMode().set(true);
                });

        // Standalone entrypoint : TODO
        TaskProvider<Jar> platformJar = target.getTasks().register("platformJar", Jar.class, jar -> {
            jar.setGroup("mclib2");

            jar.getArchiveBaseName().set(target.getName());

            jar.from(codeJar, copy -> copy.into("libs"));
            jar.from(descriptor, copy -> copy.into(".").rename(f -> "plugin-descriptor.dat"));

            jar.from(target.provider(() ->
                    runtime.getResolvedConfiguration()
                            .getResolvedArtifacts().stream()
                            .map(a -> a.getFile().toPath())
                            .collect(Collectors.toList())
            ), copy -> copy.rename((f) -> {
                ModuleVersionIdentifier id = runtime.getResolvedConfiguration()
                        .getResolvedArtifacts().stream()
                        .filter(a -> a.getFile().getName().equals(f))
                        .findFirst()
                        .map(a -> a.getModuleVersion().getId())
                        .orElse(null);

                String coordinates = id.getGroup() + ":" + id.getName();
                return coordinates.replace(":", ".") + "-" + id.getVersion() + ".jar";

            }).into("libs"));
        });

        TaskProvider<Jar> standaloneJar = target.getTasks().register("standaloneJar", Jar.class, jar -> {
            jar.setGroup("mclib2");

            jar.getArchiveBaseName().set(target.getName() + "-standalone");

            Configuration bootstrapStandalone = createConfiguration(target, "MCLib2BootstrapStandalone");
            bootstrapStandalone.getDependencies().add(dRes.resolve("be.theking90000.mclib2:platform-standalone-adapter", "be.theking90000.mclib2:standalone-adapter"));

            jar.dependsOn(bootstrapStandalone);

            jar.from(target.provider(() -> target.zipTree(platformJar.get().getArchiveFile().get().getAsFile())), copy -> copy.into("."));

            jar.from(target.provider(() ->
                    bootstrapStandalone.resolve().stream()
                            .map(target::zipTree)
                            .collect(Collectors.toList())
            ));

            jar.doFirst(task -> {
                try {
                    for (File depJar : bootstrapStandalone.resolve()) {
                        try (JarFile jf = new JarFile(depJar)) {
                            Manifest depManifest = jf.getManifest();
                            if (depManifest != null) {
                                // Clear Gradle’s default manifest
                                jar.getManifest().getAttributes().clear();

                                // Copy all attributes from dependency manifest
                                depManifest.getMainAttributes().forEach((key, value) -> {
                                    jar.getManifest().getAttributes().put(key.toString(), value);
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to read manifest from dependency", e);
                }

            });
        });


        TaskProvider<Jar> bukkitJar = target.getTasks().register("bukkitJar", Jar.class, jar -> {
            jar.setGroup("mclib2");

            jar.getArchiveBaseName().set(target.getName() + "-bukkit");

            Configuration bootstrapBukkit = createConfiguration(target, "MCLib2BootstrapBukkit");
            bootstrapBukkit.getDependencies().add(dRes.resolve("be.theking90000.mclib2:platform-bukkit-adapter", "be.theking90000.mclib2:bukkit-adapter"));

            jar.dependsOn(bootstrapBukkit);

            jar.from(target.provider(() -> target.zipTree(platformJar.get().getArchiveFile().get().getAsFile())), copy -> copy.into("."));

            jar.from(target.provider(() ->
                    bootstrapBukkit.resolve().stream()
                            .map(target::zipTree)
                            .collect(Collectors.toList())
            ));

            platformJar.get().getArchiveFile();

            jar.doFirst(task -> {
                Map<Object, Object> map = new HashMap<>();
                try {
                    for (File f : bootstrapBukkit.resolve()) {
                        Map<Object, Object> n = pluginFile(f);
                        if (n != null) {
                            map.putAll(n);
                        }
                    }
                    File generated = new File(jar.getTemporaryDir(), "plugin.yml");
                    try (Writer w = new FileWriter(generated)) {
                        new Yaml().dump(map, w);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to write merged plugin.yml", e);
                    }

                    // Put merged file at root of jar
                    jar.from(generated, spec -> spec.into(""));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to read manifest from dependency", e);
                }
            });
        });
    }

    public static String getVersion() {
        return GradlePlugin.class.getPackage().getImplementationVersion();
    }

    private Configuration createConfiguration(Project project, String name) {
        Configuration configuration = project.getConfigurations().create(name);
        configuration.setCanBeConsumed(false);
        configuration.setCanBeResolved(true);
        configuration.setVisible(false);
        return configuration;
    }

    private Map<Object, Object> pluginFile(File file) {
        try (JarFile jf = new JarFile(file)) {
            java.util.jar.JarEntry entry = jf.getJarEntry("plugin.yml");
            if (entry != null) {
                Yaml yaml = new Yaml();
                try (InputStream in = jf.getInputStream(entry)) {
                    return yaml.load(in);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read manifest from dependency: " + file, e);
        }
        return null;
    }

}
