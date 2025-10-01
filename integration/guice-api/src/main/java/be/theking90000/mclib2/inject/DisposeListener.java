package be.theking90000.mclib2.inject;

public interface DisposeListener {

    /**
     * Called when an instance is disposed.
     * This can be used to clean up resources or perform any necessary actions before the instance is discarded
     * using dispose method (if it implements Disposable).
     * @param instance the instance being disposed
     * @param <T> the type of the instance
     */
    <T> void onDispose(T instance);

}
