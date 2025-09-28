package be.theking90000.mclib2.config;

import com.google.inject.Injector;

public interface ConfigProvider {

    <T> T get(Class<T> cfgClass, Injector injector);

}
