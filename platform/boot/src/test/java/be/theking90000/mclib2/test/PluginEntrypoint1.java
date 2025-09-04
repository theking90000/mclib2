package be.theking90000.mclib2.test;

import be.theking90000.mclib2.platform.PlatformEntrypoint;

public class PluginEntrypoint1 {

    public static int init = 0;

    @PlatformEntrypoint
    public PluginEntrypoint1() {
        init++;


        try {
            Class<?> cls = Class.forName("org.slf4j.Logger");

            System.out.println("SLF4J loaded successfully in PluginEntrypoint1: " + cls);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load slf4j in PluginEntrypoint1", e);
        }
    }

}
