package be.theking90000.mclib2.config;

public interface ConfigLoader {

    /**
     * Load the config of the given class
     * @param cfgClass the config class
     * @return the loaded config
     * @param <T> the type of the config class
     */
    <T> T load(Class<T> cfgClass);

    /**
     * Save the given config
     * @param config the config to save
     * @param <T> the type of the config
     */
    <T> void save(T config);

    /**
     * Check if this loader supports the given config class
     * @param cfgClass the config class
     * @return true if this loader supports the given config class
     */
    boolean supports(Class<?> cfgClass);

}
