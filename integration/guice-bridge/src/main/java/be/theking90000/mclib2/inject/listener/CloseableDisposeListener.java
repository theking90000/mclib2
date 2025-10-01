package be.theking90000.mclib2.inject.listener;

import be.theking90000.mclib2.inject.Disposable;
import be.theking90000.mclib2.inject.DisposeListener;

public class CloseableDisposeListener implements DisposeListener {

    @Override
    public <T> void onDispose(T instance) {
        if (instance instanceof AutoCloseable) {
            try {
                ((AutoCloseable) instance).close();
            } catch (Exception e) {
                throw new RuntimeException("Failed to close instance of type " + instance.getClass(), e);
            }
        } else if (instance instanceof Disposable) {
            ((Disposable) instance).dispose();
        }
    }

}
