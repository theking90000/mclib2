package be.theking90000.mclib2.integration.bukkit.listener;

import be.theking90000.mclib2.annotations.AnnotationLoader;
import be.theking90000.mclib2.annotations.InjectStrategy;
import be.theking90000.mclib2.integration.bukkit.BukkitListener;
import be.theking90000.mclib2.integration.bukkit.PlayerScoped;
import be.theking90000.mclib2.integration.bukkit.player.PlayerScope;
import be.theking90000.mclib2.integration.guice.GuiceModuleAnnotationHandlerFactory;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.google.inject.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

@AnnotationLoader(BukkitListener.class)
@InjectStrategy(GuiceModuleAnnotationHandlerFactory.class)
public class BukkitListenerBinder implements AnnotationHandler<Listener> {

    private final Set<Module> modules;

    public BukkitListenerBinder(Set<Module> modules) {
        this.modules = modules;
    }

    @Override
    public void handle(Class<? extends Listener> clazz) throws Exception {
        if(clazz.isAnnotationPresent(PlayerScoped.class))
            return;

        modules.add(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(clazz).in(Singleton.class);
            }
        });

    }




}
