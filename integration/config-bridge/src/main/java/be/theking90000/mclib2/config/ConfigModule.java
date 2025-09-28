package be.theking90000.mclib2.config;

import be.theking90000.mclib2.integration.GuiceModule;
import com.google.inject.AbstractModule;

@GuiceModule
public class ConfigModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ConfigResolver.class).toProvider(ConfigResolverProvider.class);

        bind(ConfigLoader.class).toProvider(ConfigLoaderProvider.class);
    }


}
