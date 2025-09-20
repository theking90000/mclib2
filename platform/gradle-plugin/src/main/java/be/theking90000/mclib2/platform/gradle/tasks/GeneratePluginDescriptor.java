package be.theking90000.mclib2.platform.gradle.tasks;

import be.theking90000.mclib2.platform.PluginDescriptor;
import be.theking90000.mclib2.platform.gradle.generator.PluginDescriptorGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.FileOutputStream;

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
        PluginDescriptorGenerator generator = new PluginDescriptorGenerator(this.getDevMode().getOrElse(false));

        PluginDescriptor descriptor = generator.generate(
                getRuntimeClasspath().getFiles(),
                configuration().getResolvedConfiguration().getResolvedArtifacts(),
                codeFile());

        File file = getOutputFile().get().getAsFile();
        file.getParentFile().mkdirs();
        try (FileOutputStream os = new FileOutputStream(file)) {
            descriptor.serialize(os);
        }
    }

    private Configuration configuration() {
        return getProject().getConfigurations().getByName(getConfiguration().get());
    }

    private PluginDescriptorGenerator.CodeFile codeFile() {
        return new PluginDescriptorGenerator.CodeFile(
                getProject().getGroup().toString(),
                getProject().getName(),
                getProject().getVersion().toString()
        );
    }

}
