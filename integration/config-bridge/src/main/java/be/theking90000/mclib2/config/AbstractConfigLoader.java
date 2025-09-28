package be.theking90000.mclib2.config;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

public abstract class AbstractConfigLoader extends JsonSchemaLoader implements ConfigLoader {

    @Override
    public <T> T load(Class<T> cfgClass) {
        T config = tryLoad(cfgClass);
        if (config == null) {
            config = loadDefaults(cfgClass);
            save(config);
        }
        if (config == null) {
            throw new IllegalStateException("Could not load config for class " + cfgClass.getName());
        }
        return config;
    }

    /**
     * Load the default config of the given class
     * @param cfgClass the config class
     * @return the default config, or null if not supported
     * @param <T> the type of the config class
     */
    public @NotNull <T> T loadDefaults(Class<T> cfgClass) {
        try {
            return cfgClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Could not create default config for class " + cfgClass.getName(), e);
        }
    }

    /**
     * Try to load the config of the given class, return null if not findable or not supported
     * @param cfgClass the config class
     * @return the loaded config, or null if not findable or not supported
     * @param <T> the type of the config class
     */
    public abstract @Nullable <T> T tryLoad(Class<T> cfgClass);

    @Override
    public boolean supports(Class<?> cfgClass) {
        return true;
    }

    protected String name(Class<?> cfgClass) {
        Config cfg = cfgClass.getAnnotation(Config.class);
        if (cfg == null)
            throw new IllegalArgumentException("Class " + cfgClass.getName() + " is not annotated with @Config");

        return cfg.name();
    }
}
