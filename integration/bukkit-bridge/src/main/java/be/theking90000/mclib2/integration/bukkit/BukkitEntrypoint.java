package be.theking90000.mclib2.integration.bukkit;

import be.theking90000.mclib2.platform.PlatformEntrypoint;
import be.theking90000.mclib2.runtime.AnnotationBootstrap;
import be.theking90000.mclib2.runtime.AnnotationDiscovery;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitEntrypoint {

    @PlatformEntrypoint
    public BukkitEntrypoint(JavaPlugin plugin) {
        plugin.getLogger().info("Hello From BukkitEntrypoint>2 !");

        AnnotationDiscovery ad = new AnnotationDiscovery();
        AnnotationDiscovery.AnnotationResult ar = ad.discover();

        plugin.getLogger().info("Discovery = " + ar.getDiscoveredAnnotations().size());
        AnnotationBootstrap bs = new AnnotationBootstrap();

        bs.bootstrap(ar);
    }

}
