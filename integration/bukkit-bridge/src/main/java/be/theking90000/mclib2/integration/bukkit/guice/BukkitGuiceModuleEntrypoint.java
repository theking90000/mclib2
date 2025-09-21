package be.theking90000.mclib2.integration.bukkit.guice;

import be.theking90000.mclib2.platform.PlatformDestroy;
import be.theking90000.mclib2.platform.PlatformEntrypoint;
import be.theking90000.mclib2.platform.PlatformStore;
import be.theking90000.mclib2.runtime.AnnotationBootstrap;
import be.theking90000.mclib2.runtime.AnnotationDiscovery;
import com.google.inject.Module;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class BukkitGuiceModuleEntrypoint {

    private final AnnotationBootstrap bs;

    @PlatformEntrypoint
    public BukkitGuiceModuleEntrypoint(JavaPlugin plugin) {
        Set<Module> modules = PlatformStore.computeIfAbsent("guiceModules", HashSet::new);

        plugin.getLogger().info("Hello From BukkitEntrypoint>2 !");

        AnnotationDiscovery.AnnotationResult ar = PlatformStore.computeIfAbsent("annotationResult", () -> new AnnotationDiscovery().discover());

        plugin.getLogger().info("Discovery = " + ar.getDiscoveredAnnotations().size());
        bs = new AnnotationBootstrap(
                new BukkitGuiceModuleAnnotationHandlerFactory(plugin, modules)
        );

        bs.bootstrap(ar);
    }

    @PlatformDestroy
    public void destroy() {
        bs.shutdown();
    }

}
