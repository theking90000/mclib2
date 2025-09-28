package be.theking90000.mclib2.config;

import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.Set;

public class ConfigResolverProvider implements Provider<ConfigResolver> {

    private final Set<ConfigResolver> resolvers;

    @Inject
    public ConfigResolverProvider(Set<ConfigResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public ConfigResolver get() {
        for (ConfigResolver resolver : resolvers) {
           // if (resolver.isApplicable()) {
                return resolver;
           // }
        }
        throw new IllegalStateException("No applicable ConfigResolver found!");
    }


}
