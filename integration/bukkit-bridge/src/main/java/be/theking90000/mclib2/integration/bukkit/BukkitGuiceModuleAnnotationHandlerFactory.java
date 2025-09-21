package be.theking90000.mclib2.integration.bukkit;


import be.theking90000.mclib2.runtime.AbstractAnnotationHandlerFactory;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.google.inject.Module;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class BukkitGuiceModuleAnnotationHandlerFactory extends AbstractAnnotationHandlerFactory {

    private final JavaPlugin plugin;
    private final Set<Module> modules;

    public BukkitGuiceModuleAnnotationHandlerFactory(JavaPlugin plugin, Set<Module> modules) {
        this.plugin = plugin;
        this.modules = modules;
    }

    @Override
    public <T extends AnnotationHandler<V>, V> T create(Class<T> loaderClass) throws Exception {
        return loaderClass.getConstructor(JavaPlugin.class, Set.class).newInstance(plugin, modules);
    }

    @Override
    public <T extends AnnotationHandler<V>, V> void destroy(T handler) throws Exception {
        handler.destroy();
    }
}
