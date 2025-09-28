package be.theking90000.mclib2.config.yaml;

import be.theking90000.mclib2.config.AbstractConfigLoader;
import be.theking90000.mclib2.config.ConfigResolver;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.networknt.schema.InputFormat;
import com.networknt.schema.ValidationMessage;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

public class YamlConfigLoader extends AbstractConfigLoader {

    private final ConfigResolver resolver;

    private final ObjectMapper mapper =
            new YAMLMapper(YAMLMapper.builder()
                    .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                    .enable(JsonParser.Feature.ALLOW_COMMENTS, JsonParser.Feature.ALLOW_YAML_COMMENTS)
                    .build());

    @Inject
    public YamlConfigLoader(ConfigResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public <T> T tryLoad(Class<T> cfgClass) {
        try {
            String data = resolver.resolveAsString(name(cfgClass)+".yaml");
            if(data == null) return null;

            Set<ValidationMessage> messages =  getSchema(cfgClass).validate(data, InputFormat.YAML);
            if (!messages.isEmpty()) {
                String msg = messages.stream().map(ValidationMessage::getMessage).reduce((a, b) -> a + ", " + b).orElse("");
                throw new IllegalStateException("Config validation failed for class " + cfgClass.getCanonicalName()+" with messages: " + msg);
            }

            return mapper.readValue(data, cfgClass);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public <T> void save(T config) {
        try(OutputStream os = resolver.store(name(config.getClass())+".yaml")) {
            if(os == null) throw new IllegalStateException("Could not get output stream for config " + config.getClass().getName());
            mapper.writerWithDefaultPrettyPrinter().writeValue(os, config);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save config for class " + config.getClass().getName(), e);
        }
    }
}
