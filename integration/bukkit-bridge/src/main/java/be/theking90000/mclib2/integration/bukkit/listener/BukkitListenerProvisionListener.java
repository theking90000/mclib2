package be.theking90000.mclib2.integration.bukkit.listener;

import be.theking90000.mclib2.inject.DisposeListener;
import com.google.inject.Inject;
import com.google.inject.spi.ProvisionListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitListenerProvisionListener implements ProvisionListener, DisposeListener {

    @Inject
    public BukkitListenerRegistry registry;

    @Override
    public <T> void onProvision(ProvisionInvocation<T> provision) {
        T instance = provision.provision();

        if (!(instance instanceof Listener))
            return;

        registry.createEntry((Listener) instance).register();
    }

    @Override
    public <T> void onDispose(T instance) {
        if(!(instance instanceof Listener))
            return;

        BukkitListenerEntry entry = registry.removeEntry((Listener) instance);
        if(entry != null) {
            entry.unregister();
        }
    }
}
