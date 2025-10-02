package be.theking90000.mclib2.integration.guice;

import be.theking90000.mclib2.inject.CloseableInjector;
import be.theking90000.mclib2.inject.CloseableInjectorImpl;
import be.theking90000.mclib2.integration.scope.ScopeManager;
import be.theking90000.mclib2.platform.*;
import be.theking90000.mclib2.runtime.AnnotationBootstrap;
import be.theking90000.mclib2.runtime.AnnotationDiscovery;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;

import java.util.Set;
import java.util.logging.Logger;

public class GuiceInjectorEntrypoint {

    private final AnnotationBootstrap bs;
    private final CloseableInjector injector;

    private final Logger logger = Logger.getLogger(GuiceInjectorEntrypoint.class.getName());

    @PlatformEntrypoint
    @EntrypointPriority(Priority.HIGH)
    public GuiceInjectorEntrypoint() {
        Set<Module> modules = PlatformStore.get("guiceModules");

        logger.fine("Creating Guice Injector (modules count=" + modules.size() + ")");
        injector = CloseableInjectorImpl.createInjector(Stage.PRODUCTION, modules);

        logger.finest("Injector created" + injector);

        PlatformStore.put("guiceInjector", injector);

        AnnotationDiscovery.AnnotationResult ar = PlatformStore.computeIfAbsent("annotationResult", () -> new AnnotationDiscovery().discover());

        bs = new AnnotationBootstrap(
                new GuiceInjectorAnnotationHandlerFactory(injector)
        );

        bs.bootstrap(ar);

        ScopeManager manager = injector.getInstance(ScopeManager.class);
        manager.notifyScopeCreation(Scopes.SINGLETON); // By default Singleton is on PlatformEntrypoint
    }

    @PlatformDestroy
    public void destroy() {
        logger.fine("Shutting down Guice Injector");
        injector.close();
        bs.shutdown();
    }

}
