package be.theking90000.mclib2.test.config;

import be.theking90000.mclib2.config.Config;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Config
public class MainConfig {

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

}
