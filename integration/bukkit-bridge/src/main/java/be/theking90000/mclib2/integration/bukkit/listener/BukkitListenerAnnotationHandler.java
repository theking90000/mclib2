package be.theking90000.mclib2.integration.bukkit.listener;

import be.theking90000.mclib2.annotations.AnnotationLoader;
import be.theking90000.mclib2.annotations.InjectStrategy;
import be.theking90000.mclib2.inject.CloseableInjector;
import be.theking90000.mclib2.inject.Disposable;
import be.theking90000.mclib2.integration.bukkit.BukkitListener;
import be.theking90000.mclib2.integration.bukkit.player.PlayerScope;
import be.theking90000.mclib2.integration.guice.GuiceInjectorAnnotationHandlerFactory;
import be.theking90000.mclib2.integration.scope.ScopeCreationListener;
import be.theking90000.mclib2.integration.scope.ScopeDeletionListener;
import be.theking90000.mclib2.integration.scope.ScopeManager;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.google.inject.Inject;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import org.bukkit.event.Listener;

import java.util.*;

@AnnotationLoader(BukkitListener.class)
@InjectStrategy(GuiceInjectorAnnotationHandlerFactory.class)
public class BukkitListenerAnnotationHandler implements AnnotationHandler<Listener>,
        ScopeCreationListener, ScopeDeletionListener, Disposable {

    private final CloseableInjector injector;
    private final PlayerScope playerScope;
    private final ScopeManager scopeManager;

    private final Set<Listener> listenersInstances = new HashSet<>();
    private final Set<Class<? extends Listener>> listenerClasses = new HashSet<>();

    private final Set<Class<? extends Listener>> playerListenerClasses = new HashSet<>();
    private final Map<UUID, Set<Listener>> playerListeners = new HashMap<>();

    @Inject
    public BukkitListenerAnnotationHandler(CloseableInjector injector, PlayerScope playerScope, ScopeManager scopeManager) {
        this.injector = injector;
        this.playerScope = playerScope;
        this.scopeManager = scopeManager;

        this.scopeManager.addListener((ScopeCreationListener) this);
        this.scopeManager.addListener((ScopeDeletionListener) this);
    }

    @Override
    public void handle(Class<? extends Listener> clazz) throws Exception {
        // TODO: use scope initializer
        if (!Listener.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not a Bukkit Listener");

        if (playerScope.isPlayerScoped(clazz)) {
            playerListenerClasses.add(clazz);
        } else {
            listenerClasses.add(clazz);
            // listenersInstances.add(injector.getInstance(clazz));
        }
    }

    @Override
    public void destroy() throws Exception {
        for (Listener listener : listenersInstances) {
            injector.close(listener);
        }
        listenersInstances.clear();
        listenerClasses.clear();

        for (Set<Listener> listeners : playerListeners.values()) {
            for (Listener listener : listeners) {
                injector.close(listener);
            }
        }
        playerListeners.clear();
        playerListenerClasses.clear();
    }

    @Override
    public void onScopeCreation(Scope scope) {
        if (scope == Scopes.SINGLETON) {
            for (Class<? extends Listener> clazz : listenerClasses)
                listenersInstances.add(injector.getInstance(clazz));
        } else if(scope == playerScope) {
            UUID player =  ((PlayerScope) scope).getCurrentPlayer();
            if (player == null)
                throw new IllegalStateException("Player scope is not loaded!");

            if (playerListeners.containsKey(player))
                throw new IllegalStateException("Player scope is already created");

            Set<Listener> s = new HashSet<>();
            for (Class<? extends Listener> c : playerListenerClasses) {
                s.add(injector.getInstance(c));
            }

            playerListeners.put(player, s);
        }
    }

    @Override
    public void onScopeDeletion(Scope scope) {
        if (scope == Scopes.SINGLETON) {
            for (Listener listener : listenersInstances)
                injector.close(listener);
            listenersInstances.clear();
        } else if (scope == playerScope) {
            UUID player =  ((PlayerScope) scope).getCurrentPlayer();
            if (player == null)
                throw new IllegalStateException("Player scope is not loaded!");

            if (!playerListeners.containsKey(player))
                throw new IllegalStateException("Player scope is not loaded");

            for(Listener s : playerListeners.remove(player)) {
                injector.close(s);
            }
        }
    }

    @Override
    public void dispose() {
        this.scopeManager.removeListener((ScopeCreationListener) this);
        this.scopeManager.removeListener((ScopeDeletionListener) this);
    }
}
