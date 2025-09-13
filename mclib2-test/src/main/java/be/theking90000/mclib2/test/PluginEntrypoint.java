package be.theking90000.mclib2.test;

import be.theking90000.mclib2.platform.PlatformEntrypoint;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginEntrypoint {

    @PlatformEntrypoint
    public PluginEntrypoint(JavaPlugin plugin) {
        plugin.getLogger().info("Hello From PluginEntrypoint!");
    }

}
