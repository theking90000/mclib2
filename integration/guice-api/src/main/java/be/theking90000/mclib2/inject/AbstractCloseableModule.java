package be.theking90000.mclib2.inject;

import com.google.inject.AbstractModule;

import java.util.Set;

public abstract class AbstractCloseableModule extends AbstractModule {

    private Set<DisposeListener> disposeListeners = null;

    /**
     * Bind a dispose listener to this module.
     * @param listener The listener to bind.
     */
    protected void bindListener(DisposeListener listener) {
        if(disposeListeners == null)
            return;
        disposeListeners.add(listener);
    }

    protected void injectDisposeListeners(Set<DisposeListener> disposeListeners) {
        this.disposeListeners = disposeListeners;
    }

}
