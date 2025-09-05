package be.theking90000.mclib2.platform.adapter;

import be.theking90000.mclib2.platform.PluginDescriptor;
import be.theking90000.mclib2.platform.boot.PlatformBoot;

import java.io.IOException;
import java.io.InputStream;

public class StandaloneAdapter {

    public static void main(String[] args) {
        System.out.println("StandaloneAdapter initialized");

        try {
            PluginDescriptor descriptor;
            try (InputStream in = StandaloneAdapter.class.getResourceAsStream("/plugin-descriptor.dat")) {
                descriptor = PluginDescriptor.deserialize(in);
            }

            PlatformBoot.register(descriptor, null);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load plugin descriptor");
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Starting PlatformBoot");
        PlatformBoot.boot();
    }

}
