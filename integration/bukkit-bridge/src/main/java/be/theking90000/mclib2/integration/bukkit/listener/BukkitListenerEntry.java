package be.theking90000.mclib2.integration.bukkit.listener;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BukkitListenerEntry {

    private final BukkitListenerRegistry listenerRegistry;
    private final Set<RegisteredListenerEntry> registeredListenerEntries = new HashSet<>();
    private final Listener listener;

    protected static BukkitListenerEntry create(BukkitListenerRegistry reg, Listener lst) {
        return new BukkitListenerEntry(reg, lst);
    }

    private BukkitListenerEntry(
            BukkitListenerRegistry listenerRegistry,
            Listener listener
    ) {
        this.listenerRegistry = listenerRegistry;

        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry
                : listenerRegistry.getPlugin().getPluginLoader().createRegisteredListeners(listener, listenerRegistry.getPlugin()).entrySet()) {
            Class<? extends Event> event = entry.getKey();
            for (RegisteredListener rl : entry.getValue()) {
                registeredListenerEntries.add(new RegisteredListenerEntry(event, rl));
            }
        }

        this.listener = listener;
    }

    public Listener getListener() {
        return listener;
    }

    public void register() {
        for(RegisteredListenerEntry entry : registeredListenerEntries) {
            entry.register();
        }
    }

    public void unregister() {
        for(RegisteredListenerEntry entry : registeredListenerEntries) {
            entry.unregister();
        }
    }

    public class RegisteredListenerEntry {
        private final Class<? extends Event> event;
        private final RegisteredListener registeredListener;

        private boolean isRegistered = false;

        private RegisteredListenerEntry(Class<? extends Event> event, RegisteredListener registeredListener) {
            this.event = event;
            this.registeredListener = registeredListener;
        }

        public void register() {
            if(isRegistered) return;

            listenerRegistry.getHandlerList(event).register(registeredListener);

            isRegistered = true;
        }

        public void unregister() {
            if(!isRegistered) return;

            listenerRegistry.getHandlerList(event).unregister(registeredListener);

            isRegistered = false;
        }

    }

}
