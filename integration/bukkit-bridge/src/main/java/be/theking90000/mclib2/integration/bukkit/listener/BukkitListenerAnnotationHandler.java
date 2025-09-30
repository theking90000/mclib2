package be.theking90000.mclib2.integration.bukkit.listener;

import be.theking90000.mclib2.annotations.AnnotationLoader;
import be.theking90000.mclib2.annotations.InjectStrategy;
import be.theking90000.mclib2.integration.bukkit.BukkitListener;
import be.theking90000.mclib2.integration.bukkit.PlayerScoped;
import be.theking90000.mclib2.integration.bukkit.player.PlayerScope;
import be.theking90000.mclib2.integration.guice.GuiceInjectorAnnotationHandlerFactory;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.bukkit.event.Listener;

@AnnotationLoader(BukkitListener.class)
@InjectStrategy(GuiceInjectorAnnotationHandlerFactory.class)
public class BukkitListenerAnnotationHandler implements AnnotationHandler<Listener> {

    private final Injector injector;

    // TODO: make this GUICE singleton : allow use in @AnnotationLoader(Service.class) module
    // To "registerService" in bukkitListenerManager
    private final BukkitListenerManager bukkitListenerManager;
    private final BukkitPlayerListener bukkitPlayerListener;

    @Inject
    public BukkitListenerAnnotationHandler(Injector injector,
                                           BukkitListenerManager bukkitListenerManager,
                                           BukkitPlayerListener bukkitPlayerListener) {
        this.injector = injector;
        this.bukkitListenerManager = bukkitListenerManager;
        this.bukkitPlayerListener = bukkitPlayerListener;

        this.bukkitListenerManager.registerListener(bukkitPlayerListener);
    }

    @Override
    public void handle(Class<? extends Listener> clazz) throws Exception {
        // TODO: rework via Matchers and TypeListeners to allow any injector.getInstance(clazz) call
        if (!Listener.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not a Bukkit Listener");

        if (clazz.isAnnotationPresent(PlayerScoped.class)) {
            bukkitPlayerListener.addListenerClass(clazz);
        } else {
            bukkitListenerManager.registerListener(injector.getInstance(clazz));
        }
    }

    @Override
    public void destroy() throws Exception {
        bukkitPlayerListener.removeAllListenerClass();
        bukkitListenerManager.unregisterAllListeners();
    }


}
