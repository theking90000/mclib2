package be.theking90000.mclib2.platform.gradle;

import be.theking90000.mclib2.platform.gradle.tasks.GeneratePluginDescriptor;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvableConfiguration;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;

import java.io.File;

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
        });

        Configuration runtime = createConfiguration(target, "MCLib2Runtime");
        runtime.extendsFrom(target.getConfigurations().getByName("runtimeClasspath"));

        Configuration bootstrapLibs = createConfiguration(target, "MCLib2BootstrapLibs");

        TaskProvider<Jar> codeJar = target.getTasks().register("codeJar", Jar.class, jar -> {
            jar.getArchiveBaseName().set("code");
            SourceSetContainer sourceSets = target.getExtensions().getByType(SourceSetContainer.class);
            jar.from(sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).getOutput());
        });

        TaskProvider<GeneratePluginDescriptor> descriptor = target.getTasks()
                .register("generatePluginDescriptor", GeneratePluginDescriptor.class, task -> {
                    task.dependsOn(codeJar);

                    task.getCodeFile().set(codeJar.flatMap(Jar::getArchiveFile));

                    task.getConfiguration().set(runtime.getName());
                    task.getOutputFile().set(target.getLayout().getBuildDirectory().file("generated/plugin-descriptor.dat"));
                });

        TaskProvider<GeneratePluginDescriptor> descriptorDev = target.getTasks()
                .register("generatePluginDescriptorDev", GeneratePluginDescriptor.class, task -> {
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

}
