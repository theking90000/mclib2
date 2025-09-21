package be.theking90000.mclib2.integration.guice;

import be.theking90000.mclib2.platform.PlatformDestroy;
import be.theking90000.mclib2.platform.PlatformEntrypoint;
import be.theking90000.mclib2.platform.PlatformStore;
import be.theking90000.mclib2.runtime.AnnotationBootstrap;
import be.theking90000.mclib2.runtime.AnnotationDiscovery;
import com.google.inject.Module;

import java.util.HashSet;
import java.util.Set;

public class GuiceModuleEntrypoint {

    private final AnnotationBootstrap bs;

    @PlatformEntrypoint
    public GuiceModuleEntrypoint() {
        Set<Module> modules = PlatformStore.computeIfAbsent("guiceModules", HashSet::new);

        AnnotationDiscovery.AnnotationResult ar = PlatformStore.computeIfAbsent("annotationResult", () -> new AnnotationDiscovery().discover());

        bs = new AnnotationBootstrap(
                new GuiceModuleAnnotationHandlerFactory(modules)
        );

        bs.bootstrap(ar);

        System.out.println("Modules = " + modules.size());
    }

    @PlatformDestroy
    public void destroy() {
        bs.shutdown();
    }

}
