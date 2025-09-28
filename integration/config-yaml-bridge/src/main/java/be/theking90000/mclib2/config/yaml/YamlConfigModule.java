package be.theking90000.mclib2.config.yaml;

import be.theking90000.mclib2.config.ConfigLoader;
import be.theking90000.mclib2.integration.GuiceModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

@GuiceModule
public class YamlConfigModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ConfigLoader.class)
                .addBinding()
                .to(YamlConfigLoader.class);
    }
}
