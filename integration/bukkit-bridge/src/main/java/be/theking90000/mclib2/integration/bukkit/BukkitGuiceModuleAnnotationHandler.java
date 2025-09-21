package be.theking90000.mclib2.integration.bukkit;

import be.theking90000.mclib2.annotations.AnnotationLoader;
import be.theking90000.mclib2.annotations.InjectStrategy;
import be.theking90000.mclib2.integration.GuiceModule;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.google.inject.Module;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

@AnnotationLoader(GuiceModule.class)
@InjectStrategy(BukkitGuiceModuleAnnotationHandlerFactory.class)
public class BukkitGuiceModuleAnnotationHandler implements AnnotationHandler<Module> {

    private final JavaPlugin javaPlugin;
    private final Set<Module> modules;

    public BukkitGuiceModuleAnnotationHandler(JavaPlugin javaPlugin, Set<Module> modules) {
        this.javaPlugin = javaPlugin;
        this.modules = modules;
    }

    @Override
    public void handle(Class<? extends Module> clazz) throws Exception {
        try {
            modules.add(clazz.getDeclaredConstructor(JavaPlugin.class).newInstance(javaPlugin));
        } catch (NoSuchMethodException e) {
            //
        }
    }
}
