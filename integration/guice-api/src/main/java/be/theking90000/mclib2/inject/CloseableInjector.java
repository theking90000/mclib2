package be.theking90000.mclib2.inject;

import com.google.inject.Injector;

public interface CloseableInjector extends Injector {

    /**
     * Attempts to close the given instance and all of its dependent resources that implement {@code Disposable},
     * provided they are no longer in use. This does not apply to resources annotated with {@code @Singleton};
     * see the {@link #close()} method for closing all singletons.
     *
     * @param instance the instance to close along with its unused dependencies
     * @param <T>      the type of the instance
     */
    <T> void close(T instance);

    /**
     * Closes all resources (including singleton) managed by this injector that implement {@code Disposable}.
     * This method is intended to release resources annotated with {@code @Singleton}.
     * Once this method is called, the injector should not be used anymore.
     */
    void close();

}
