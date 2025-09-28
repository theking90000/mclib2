package be.theking90000.mclib2.config;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

public class ConfigDynamicProvider implements Provider<Object> {

    private ConfigProvider provider;
    private final Class<?> clazz;

    @Inject private Injector injector;
    @Inject private GlobalConfigProvider globalConfigProvider;

    public ConfigDynamicProvider(Class<?> clazz, ConfigProvider provider) {
        this.provider = provider;
        this.clazz = clazz;
    }

    public ConfigDynamicProvider(Class<?> clazz) {
        this(clazz, null);
    }

    private ConfigProvider getProvider() {
        return this.provider != null ? this.provider : globalConfigProvider.getProvider();
    }

    @Override
    public Object get() {
        return getProvider().get(clazz, injector);
    }
}
