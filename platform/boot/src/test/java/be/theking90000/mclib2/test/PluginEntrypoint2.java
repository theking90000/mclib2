package be.theking90000.mclib2.test;

import be.theking90000.mclib2.platform.PlatformEntrypoint;

public class PluginEntrypoint2 {

    @PlatformEntrypoint
    public PluginEntrypoint2() {
        try {
            Class<?> cls = Class.forName("org.slf4j.Logger");

            System.out.println("SLF4J loaded successfully in PluginEntrypoint2: " + cls);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load slf4j in PluginEntrypoint2", e);
        }

        try {
            // MCP is not present in 1.0
            // And PluginEntrypoint2 is only loaded in 1.0
            Class<?> cls = Class.forName("org.slf4j.MCP");

        } catch (ClassNotFoundException e) {
            System.out.println("SLF4J loaded successfully in PluginEntrypoint2: ");
            return;
        }

        throw new RuntimeException("Failed to load slf4j in PluginEntrypoint2");
    }

}
