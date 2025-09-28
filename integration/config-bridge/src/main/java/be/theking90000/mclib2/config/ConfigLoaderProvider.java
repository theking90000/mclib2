package be.theking90000.mclib2.config;

import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.Set;

public class ConfigLoaderProvider implements Provider<ConfigLoader> {

    private final Set<ConfigLoader> loaders;

    @Inject
    public ConfigLoaderProvider(Set<ConfigLoader> loaders) {
        this.loaders = loaders;
    }

    public ConfigLoader get() {
        for (ConfigLoader loader : loaders) {
           // if (resolver.isApplicable()) {
                return loader;
           // }
        }
        throw new IllegalStateException("No applicable ConfigLoader found!");
    }


}
