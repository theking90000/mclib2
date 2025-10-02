package be.theking90000.mclib2.integration.scope;

import com.google.inject.Inject;
import com.google.inject.Scope;
import com.google.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

@Singleton
public class ScopeCreationImpl implements ScopeCreation {

    private final Set<ScopeCreationListener> creationListeners = new HashSet<>();
    private final Set<ScopeDeletionListener> deletionListeners = new HashSet<>();

    public ScopeCreationImpl() {}

    @Override
    public void addListener(ScopeCreationListener creationListener) {
        creationListeners.add(creationListener);
    }

    @Override
    public void addListener(ScopeDeletionListener deletionListener) {
        deletionListeners.add(deletionListener);
    }

    @Override
    public void removeListener(ScopeCreationListener creationListener) {
        creationListeners.remove(creationListener);
    }

    @Override
    public void removeListener(ScopeDeletionListener deletionListener) {
        deletionListeners.remove(deletionListener);
    }

    @Override
    public void notifyScopeCreation(Scope scope) {
        for (ScopeCreationListener lst : creationListeners) {
            lst.onScopeCreation(scope);
        }
    }

    @Override
    public void notifyScopeDeletion(Scope scope) {
        for(ScopeDeletionListener lst : deletionListeners) {
            lst.onScopeDeletion(scope);
        }
    }
}
