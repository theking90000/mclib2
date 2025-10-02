package be.theking90000.mclib2.integration.bukkit.listener;

import be.theking90000.mclib2.inject.DisposeListener;
import be.theking90000.mclib2.integration.bukkit.player.PlayerScope;
import com.google.inject.Inject;
import com.google.inject.spi.ProvisionListener;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BukkitListenerProvisionListener implements ProvisionListener, DisposeListener {

    @Inject
    public BukkitListenerRegistry registry;

    @Inject
    public BukkitPlayerListener playerListener;

    @Inject
    public PlayerScope playerScope;

    @Override
    public <T> void onProvision(ProvisionInvocation<T> provision) {
        if(!Listener.class.isAssignableFrom(provision.getBinding().getKey().getTypeLiteral().getRawType()))
            return;

        T instance = provision.provision();

        if(playerScope.isPlayerScoped(provision.getBinding())) {
            UUID uuid = playerScope.getCurrentPlayer();
            if(uuid == null)
                throw new RuntimeException("Player scope is not loaded");

            playerListener.addListener(uuid, (Listener) instance);
        } else {
            registry.createEntry((Listener) instance).register();
        }
    }

    @Override
    public <T> void onDispose(T instance) {
        if(!(instance instanceof Listener))
            return;

        if(playerScope.isPlayerScoped(instance.getClass())) {
            UUID uuid = playerScope.getCurrentPlayer();
            if(uuid == null)
                throw new RuntimeException("Player scope is not loaded");

            playerListener.removeListener(uuid, (Listener) instance);
        } else {
            BukkitListenerEntry entry = registry.removeEntry((Listener) instance);
            if(entry != null) {
                entry.unregister();
            }
        }
    }
}
