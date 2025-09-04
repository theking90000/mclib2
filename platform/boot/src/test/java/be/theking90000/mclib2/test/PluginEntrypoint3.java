package be.theking90000.mclib2.test;

import be.theking90000.mclib2.platform.PlatformEntrypoint;

public class PluginEntrypoint3 {

    @PlatformEntrypoint
    public PluginEntrypoint3() {
        try {
            // MCP is not present in 1.0
            // And PluginEntrypoint3 is only loaded in 1.0
            Class<?> cls = Class.forName("org.slf4j.MCP");

        } catch (ClassNotFoundException e) {
            System.out.println("SLF4J loaded successfully in PluginEntrypoint3: ");
            return;
        }

        throw new RuntimeException("Failed to load slf4j in PluginEntrypoint3");
    }

}
