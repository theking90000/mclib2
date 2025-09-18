package be.theking90000.mclib2.platform.gradle.tasks;

import be.theking90000.mclib2.platform.gradle.utils.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

public abstract class PluginYaml extends DefaultTask {

    @InputFiles
    public abstract ConfigurableFileCollection getClasspath();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    private final Yaml yaml;

    public PluginYaml() {
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        this.yaml = new Yaml(options);
    }

    @TaskAction
    public void generate() throws Exception {
        Map<Object, Object> values = new HashMap<>();
        for (File file : getClasspath().getFiles()) {
            values.putAll(pluginFile(file));
        }

        if(!values.containsValue("version")) {
            values.put("version", getProject().getVersion().toString());
        }

        File file = getOutputFile().get().getAsFile();
        file.getParentFile().mkdirs();
        try (FileWriter w = new FileWriter(file)) {
           yaml.dump(values, w);
        }
    }

    private Map<Object, Object> pluginFile(File file) throws IOException {
        try(InputStream in = FileUtils.getFileFromJarOrDirectory(file, "plugin.yml")) {
            if (in != null) {
                return yaml.load(in);
            }
        }
        return Collections.emptyMap();
    }

}
