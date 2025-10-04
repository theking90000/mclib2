package be.theking90000.mclib2.platform.adapter;

import be.theking90000.mclib2.platform.PluginDescriptor;
import be.theking90000.mclib2.platform.boot.PlatformBoot;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class BukkitAdapter extends JavaPlugin implements Runnable {

    private PluginDescriptor descriptor;

    @Override
    public void onEnable() {
        getLogger().info("BukkitAdapter loading");
        try {
            URL url = BukkitAdapter.class.getClassLoader().getResource("plugin-descriptor.dat");
            assert url != null;
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            try (InputStream in = conn.getInputStream()) {
                descriptor = PluginDescriptor.deserialize(in);
            }

            PlatformBoot.register(descriptor, this, getClassLoader());
        } catch (IOException | ClassNotFoundException e) {
            getLogger().severe("Failed to load plugin descriptor");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }

        // The task will run when the server is fully started
        // id est, after onEnable of all plugins has been called
        getServer().getScheduler().runTask(this, this);
    }

    @Override
    public void run() {
        PlatformBoot.boot(getClassLoader());
    }

    @Override
    public void onDisable() {
        if (descriptor != null) {
            PlatformBoot.unregister(descriptor, getClassLoader());
            descriptor = null;
        }
    }
}
