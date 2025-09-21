package be.theking90000.mclib2.integration.guice;

import be.theking90000.mclib2.platform.EntrypointPriority;
import be.theking90000.mclib2.platform.PlatformEntrypoint;
import be.theking90000.mclib2.platform.PlatformStore;
import be.theking90000.mclib2.platform.Priority;
import be.theking90000.mclib2.runtime.AnnotationBootstrap;
import be.theking90000.mclib2.runtime.AnnotationDiscovery;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.util.Set;

public class GuiceInjectorEntrypoint {

    @PlatformEntrypoint
    @EntrypointPriority(Priority.HIGH)
    public GuiceInjectorEntrypoint() {
        Set<Module> modules = PlatformStore.get("guiceModules");

        System.out.println("Creating Guice Injector (modules count=" + modules.size() + ")");
        Injector injector = Guice.createInjector(modules);

        System.out.println("Injector created" + injector);

        PlatformStore.put("guiceInjector", injector);

        AnnotationDiscovery.AnnotationResult ar = PlatformStore.computeIfAbsent("annotationResult", () -> new AnnotationDiscovery().discover());

        AnnotationBootstrap bs = new AnnotationBootstrap(
                new GuiceInjectorAnnotationHandlerFactory(injector)
        );

        bs.bootstrap(ar);
    }

}
