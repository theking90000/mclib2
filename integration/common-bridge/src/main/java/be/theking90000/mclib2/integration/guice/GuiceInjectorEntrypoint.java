package be.theking90000.mclib2.integration.guice;

import be.theking90000.mclib2.inject.CloseableInjector;
import be.theking90000.mclib2.inject.CloseableInjectorImpl;
import be.theking90000.mclib2.platform.*;
import be.theking90000.mclib2.runtime.AnnotationBootstrap;
import be.theking90000.mclib2.runtime.AnnotationDiscovery;
import com.google.inject.Module;
import com.google.inject.Stage;

import java.util.Set;

public class GuiceInjectorEntrypoint {

    private final AnnotationBootstrap bs;
    private final CloseableInjector injector;

    @PlatformEntrypoint
    @EntrypointPriority(Priority.HIGH)
    public GuiceInjectorEntrypoint() {
        Set<Module> modules = PlatformStore.get("guiceModules");

        System.out.println("Creating Guice Injector (modules count=" + modules.size() + ")");
        injector = CloseableInjectorImpl.createInjector(Stage.DEVELOPMENT, modules);

        System.out.println("Injector created" + injector);

        PlatformStore.put("guiceInjector", injector);

        AnnotationDiscovery.AnnotationResult ar = PlatformStore.computeIfAbsent("annotationResult", () -> new AnnotationDiscovery().discover());

        bs = new AnnotationBootstrap(
                new GuiceInjectorAnnotationHandlerFactory(injector)
        );

        bs.bootstrap(ar);
    }

    @PlatformDestroy
    public void destroy() {
        System.out.println("Shutting down Guice Injector");
        injector.close();
        bs.shutdown();
    }

}
