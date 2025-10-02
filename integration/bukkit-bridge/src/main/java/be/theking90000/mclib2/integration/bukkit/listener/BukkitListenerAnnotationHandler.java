package be.theking90000.mclib2.integration.bukkit.listener;

import be.theking90000.mclib2.annotations.AnnotationLoader;
import be.theking90000.mclib2.annotations.InjectStrategy;
import be.theking90000.mclib2.inject.CloseableInjector;
import be.theking90000.mclib2.integration.bukkit.BukkitListener;
import be.theking90000.mclib2.integration.bukkit.PlayerScoped;
import be.theking90000.mclib2.integration.guice.GuiceInjectorAnnotationHandlerFactory;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.google.inject.Inject;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

@AnnotationLoader(BukkitListener.class)
@InjectStrategy(GuiceInjectorAnnotationHandlerFactory.class)
public class BukkitListenerAnnotationHandler implements AnnotationHandler<Listener> {

    private final CloseableInjector injector;

    private final Set<Listener> listenersInstances = new HashSet<>();

    @Inject
    public BukkitListenerAnnotationHandler(CloseableInjector injector) {
        this.injector = injector;
        /*this.bukkitListenerManager = bukkitListenerManager;
        this.bukkitPlayerListener = bukkitPlayerListener;

        this.bukkitListenerManager.registerListener(bukkitPlayerListener);*/
    }

    @Override
    public void handle(Class<? extends Listener> clazz) throws Exception {
        // TODO: use scope initializer
        if (!Listener.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not a Bukkit Listener");

        if (clazz.isAnnotationPresent(PlayerScoped.class)) {
            // bukkitPlayerListener.addListenerClass(clazz);
        } else {
            listenersInstances.add(injector.getInstance(clazz));
        }
    }

    @Override
    public void destroy() throws Exception {
        for (Listener listener : listenersInstances) {
            injector.close(listener);
        }
        listenersInstances.clear();

        // for (Class<? extends Listener> listenerClass : listenerClasses.keySet()) {
        //     bukkitPlayerListener.removeListenerClass(listenerClass);
        // }
        // listenerClasses.clear();
    }


}
