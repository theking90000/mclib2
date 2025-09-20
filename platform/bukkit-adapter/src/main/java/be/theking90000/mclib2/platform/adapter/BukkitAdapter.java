package be.theking90000.mclib2.platform.adapter;

import be.theking90000.mclib2.platform.PluginDescriptor;
import be.theking90000.mclib2.platform.boot.PlatformRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;

public class BukkitAdapter extends JavaPlugin implements Runnable {

    private PluginDescriptor descriptor;

    private PlatformRegistry registry;

    public BukkitAdapter() {

    }

    @Override
    public void onLoad() {
        registry = findServiceByClassName(PlatformRegistry.class.getCanonicalName());

        if(registry == null) {
            registry = new PlatformRegistry();
            Bukkit.getServicesManager().register(PlatformRegistry.class, registry, this, ServicePriority.Normal);
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("BukkitAdapter loading");
        try {
            try (InputStream in = BukkitAdapter.class.getClassLoader().getResourceAsStream("plugin-descriptor.dat")) {
                descriptor = PluginDescriptor.deserialize(in);
            }

            registry.register(
                    descriptor,
                    this,
                    this.getClass().getClassLoader()
            );
            // PlatformBoot.register(descriptor, this, rootCl);
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
        registry.boot();
        //PlatformBoot.boot(rootCl);
    }

    @Override
    public void onDisable() {
        if(descriptor != null) {
            registry.unregister(descriptor);
            //PlatformBoot.unregister(descriptor, rootCl);
            descriptor = null;
        }
    }

    private <T> T findServiceByClassName(String name) {
        for(Class<?> c : Bukkit.getServicesManager().getKnownServices()) {
            if(c.getCanonicalName().equals(name)) {
                return (T) Bukkit.getServicesManager().load(c);
            }
        }
        return null;
    }
}
