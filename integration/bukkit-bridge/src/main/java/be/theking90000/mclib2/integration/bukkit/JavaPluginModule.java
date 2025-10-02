package be.theking90000.mclib2.integration.bukkit;

import be.theking90000.mclib2.integration.guice.GuiceModule;
import com.google.inject.AbstractModule;
import org.bukkit.plugin.java.JavaPlugin;

@GuiceModule
public class JavaPluginModule extends AbstractModule {

    private final JavaPlugin javaPlugin;

    public JavaPluginModule(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    @Override
    protected void configure() {
        bind(JavaPlugin.class).toInstance(javaPlugin);
    }
}
