package be.theking90000.mclib2.integration.guice;

import be.theking90000.mclib2.platform.PlatformEntrypoint;
import be.theking90000.mclib2.platform.PlatformStore;
import be.theking90000.mclib2.runtime.AnnotationBootstrap;
import be.theking90000.mclib2.runtime.AnnotationDiscovery;
import com.google.inject.Module;

import java.util.HashSet;
import java.util.Set;

public class GuiceModuleEntrypoint {

    @PlatformEntrypoint
    public GuiceModuleEntrypoint() {
        Set<Module> modules = PlatformStore.computeIfAbsent("guiceModules", HashSet::new);

        AnnotationDiscovery ad = new AnnotationDiscovery();
        AnnotationDiscovery.AnnotationResult ar = ad.discover();

        AnnotationBootstrap bs = new AnnotationBootstrap(
                new GuiceModuleAnnotationHandlerFactory(modules)
        );

        bs.bootstrap(ar);

        System.out.println("Modules = " + modules.size());

        // TODO: find a way to share information accross PlatformEntrypoints?
        // Maybe Using a static Magic class and Thread.currentThread().getContextClassLoader() to define each plugin.
        // Guice.createInjector(modules);
    }

}
