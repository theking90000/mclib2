package be.theking90000.mclib2.integration.bukkit;

import be.theking90000.mclib2.platform.PlatformEntrypoint;
import be.theking90000.mclib2.runtime.AnnotationBootstrap;
import be.theking90000.mclib2.runtime.AnnotationDiscovery;
import com.google.inject.Module;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class BukkitEntrypoint {

    @PlatformEntrypoint
    public BukkitEntrypoint(JavaPlugin plugin) {
        Set<Module> modules = new HashSet<>();

        plugin.getLogger().info("Hello From BukkitEntrypoint>2 !");

        AnnotationDiscovery ad = new AnnotationDiscovery();
        AnnotationDiscovery.AnnotationResult ar = ad.discover();

        plugin.getLogger().info("Discovery = " + ar.getDiscoveredAnnotations().size());
        AnnotationBootstrap bs = new AnnotationBootstrap(
                new BukkitGuiceModuleAnnotationHandlerFactory(plugin, modules)
        );

        bs.bootstrap(ar);
    }

}
