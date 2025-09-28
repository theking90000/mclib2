package be.theking90000.mclib2.config.bukkit;

import be.theking90000.mclib2.config.ConfigResolver;
import be.theking90000.mclib2.integration.GuiceModule;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import org.bukkit.plugin.java.JavaPlugin;

@GuiceModule
public class BukkitConfigModule extends AbstractModule {

    private final JavaPlugin javaPlugin;

    @Inject
    public BukkitConfigModule(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ConfigResolver.class)
                .addBinding()
                .to(BukkitConfigResolver.class);
    }
}
