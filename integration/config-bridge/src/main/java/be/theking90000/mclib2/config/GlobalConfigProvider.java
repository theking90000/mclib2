package be.theking90000.mclib2.config;

import com.google.inject.Injector;
import jakarta.inject.Singleton;

@Singleton
public class GlobalConfigProvider {

    private ConfigProvider provider = new ConfigProvider() {
        @Override
        public <T> T get(Class<T> cfgClass, Injector injector) {
            return injector.getInstance(ConfigLoader.class).load(cfgClass);
        }
    };

    public void setProvider(ConfigProvider provider) {
        this.provider = provider;
    }

    public ConfigProvider getProvider() {
        return provider;
    }
}
