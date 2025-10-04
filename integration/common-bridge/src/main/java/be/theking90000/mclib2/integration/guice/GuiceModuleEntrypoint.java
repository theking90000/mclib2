package be.theking90000.mclib2.integration.guice;

import be.theking90000.mclib2.platform.PlatformDestroy;
import be.theking90000.mclib2.platform.PlatformEntrypoint;
import be.theking90000.mclib2.platform.PlatformStore;
import be.theking90000.mclib2.runtime.AnnotationBootstrap;
import be.theking90000.mclib2.runtime.AnnotationDiscovery;
import com.google.inject.Module;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class GuiceModuleEntrypoint {

    private static final Logger logger = Logger.getLogger(GuiceModuleEntrypoint.class.getName());

    private final AnnotationBootstrap bs;

    @PlatformEntrypoint
    public GuiceModuleEntrypoint() {
        Set<Module> modules = PlatformStore.computeIfAbsent("guiceModules", HashSet::new);

        AnnotationDiscovery.AnnotationResult ar = PlatformStore.computeIfAbsent("annotationResult", () -> new AnnotationDiscovery().discover());

        bs = new AnnotationBootstrap(
                new GuiceModuleAnnotationHandlerFactory(modules)
        );

        int m = modules.size();

        bs.bootstrap(ar);

        logger.finer("Discovered " + (modules.size()-m) + " Guice Modules");
    }

    @PlatformDestroy
    public void destroy() {
        bs.shutdown();
    }

}
