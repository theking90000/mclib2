package be.theking90000.mclib2.config.processor;

import be.theking90000.mclib2.config.Config;
import be.theking90000.mclib2.platform.gradle.plugin.ClassProcessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.victools.jsonschema.generator.*;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;

@AutoService(ClassProcessor.class)
public class ConfigSchemaGeneratorProcessor implements ClassProcessor {

    private final SchemaGenerator generator;

    public ConfigSchemaGeneratorProcessor() {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        SchemaGeneratorConfig config = configBuilder.build();
        this.generator = new SchemaGenerator(config);
    }

    @Override
    public void processClass(Class<?> c, Path outputDir) {
       // for(Class<?> c : clazz.get()) {
            for (Annotation a : c.getAnnotations()) {
                if (a.annotationType().equals(Config.class)) {
                    generateSchema(c, outputDir);
                }
            }
       // }
    }

    private void generateSchema(Class<?> configClass, Path outputDir) {
        String className = configClass.getCanonicalName();
        Path outputFile = outputDir.resolve( "schema/"+className + ".json");

        JsonNode node = this.generator.generateSchema(configClass);

        System.out.println("Generated schema for " + className);
        outputDir.resolve("schema/").toFile().mkdirs();
        ObjectMapper m = new ObjectMapper();
        ObjectWriter w = m.writer(new DefaultPrettyPrinter());

        try {
            w.writeValue(outputFile.toFile(), node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
