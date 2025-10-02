package be.theking90000.mclib2.integration.scope;

import com.google.inject.Scope;

/**
 * This interfaces manages the creation/deletion of Scopes.
 * <p>
 * It allows modules to register listener of Scope creation and deletion and
 * to notify when a new Scope is created.
 * </p>
 */
public interface ScopeManager {

    /**
     * Register a new scope creation listener
     * @param creationListener the scope creation listener to register
     */
    void addListener(ScopeCreationListener creationListener);

    /**
     * Register a new scope deletion listener
     * @param deletionListener the scope deletion listener to register
     */
    void addListener(ScopeDeletionListener deletionListener);

    /**
     * Unregister a scope creation listener
     * @param creationListener the scope creation listener to unregister
     */
    void removeListener(ScopeCreationListener creationListener);

    /**
     * Unregister a scope creation listener
     * @param deletionListener the scope deletion listener to unregister
     */
    void removeListener(ScopeDeletionListener deletionListener);

    /**
     * Notify of the creation of a new Scope
     * (for instance Scope.enter(...) )
     * <p>
     *     This will trigger all the scope creation listeners registered
     * </p>
     * @param scope the scope
     */
    void notifyScopeCreation(Scope scope);

    /**
     * Notify of the deletion of a new Scope
     * when a scope goes out of range
     * ( Scope.remove( ... ) )
     * <p>
     *     This will trigger all the scope deletion listeners registered
     * </p>
     * @param scope the scope
     */
    void notifyScopeDeletion(Scope scope);

}
