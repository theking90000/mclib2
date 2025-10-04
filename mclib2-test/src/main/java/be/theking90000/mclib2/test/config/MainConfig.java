package be.theking90000.mclib2.test.config;

import be.theking90000.mclib2.config.Config;
import be.theking90000.mclib2.inject.Disposable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.logging.Logger;

@Config(name = "main")
public class MainConfig implements Disposable {

    private static final Logger logger = Logger.getLogger(MainConfig.class.getName());

    @JsonProperty("print-startup-message")
    @JsonPropertyDescription("If true, a startup message will be printed to the console.")
    public boolean printStartupMessage = true;

    @JsonProperty("modules")
    @JsonPropertyDescription("List of modules.")
    public ModuleConfig modules = new ModuleConfig();

    public static class ModuleConfig {
        @JsonProperty("enable-feature-x")
        @JsonPropertyDescription("If true, feature X will be enabled.")
        public boolean enableFeatureX = false;
    }

    @Override
    public void dispose() {
        logger.info("Disposing MainConfig");
    }
}
