package be.theking90000.mclib2.integration.bukkit.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@Singleton
public class BukkitPlayerListener {

    private final JavaPlugin plugin;
    private final BukkitListenerRegistry registry;

    private final Map<Key, PlayerRegisteredListener> registeredListenerMap = new HashMap();

    @Inject
    public BukkitPlayerListener(JavaPlugin plugin, BukkitListenerRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public void addListener(UUID player, Listener listener) {
        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry
                : plugin.getPluginLoader().createRegisteredListeners(listener, plugin).entrySet()) {
            Class<? extends Event> event = entry.getKey();
            for (RegisteredListener rl : entry.getValue()) {
                getRegisteredPlayerListener(Key.of(event, rl)).addPlayer(player, rl);
            }
        }
    }

    public void removeListener(UUID player, Listener listener) {
        // Might not be optimized to "create"RegisteredListeners each time
        // (reflection cost) -> might check for cached approach
        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry
                : plugin.getPluginLoader().createRegisteredListeners(listener, plugin).entrySet()) {
            Class<? extends Event> event = entry.getKey();
            for (RegisteredListener rl : entry.getValue()) {
                getRegisteredPlayerListener(Key.of(event, rl)).removePlayer(player, listener);
            }
        }
    }

    private PlayerRegisteredListener getRegisteredPlayerListener(Key key) {
        return registeredListenerMap.computeIfAbsent(key, PlayerRegisteredListener::new);
    }

    public static class Key {

        private final Class<? extends Event> event;
        private final EventPriority priority;
        private final boolean ignoreCancelled;

        private Key(Class<? extends Event> event, EventPriority priority, boolean ignoreCancelled) {
            this.event = event;
            this.priority = priority;
            this.ignoreCancelled = ignoreCancelled;
        }

        public static Key of(Class<? extends Event> event, RegisteredListener registeredListener) {
            return new Key(event, registeredListener.getPriority(), registeredListener.isIgnoringCancelled());
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            Key that = (Key) o;
            return ignoreCancelled == that.ignoreCancelled && Objects.equals(event, that.event) && priority == that.priority;
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(event);
            result = 31 * result + Objects.hashCode(priority);
            result = 31 * result + Boolean.hashCode(ignoreCancelled);
            return result;
        }
    }

    public class PlayerRegisteredListener implements EventExecutor {

        private final RegisteredListener registeredListener;
        private final Key key;
        private boolean isRegistered = false;

        private final Map<UUID, Set<RegisteredListener>> players = new HashMap<>();

        protected PlayerRegisteredListener(Key key) {
            this.key = key;
            this.registeredListener = new RegisteredListener(
                    new Listener() {}, this, key.priority,
                    registry.getPlugin(), key.ignoreCancelled
            );
        }

        public void addPlayer(UUID uuid, RegisteredListener rl) {
            this.players.computeIfAbsent(uuid, (k)->new HashSet<>()).add(rl);

            if (this.players.size() == 1) register();
        }

        public void removePlayer(UUID uuid, Listener listener) {
            Set<RegisteredListener> rls = this.players.get(uuid);
            if (rls != null) {
                for (RegisteredListener rl : rls) {
                    if(rl.getListener() == listener) {
                        rls.remove(rl);
                        break;
                    }
                }
                if(rls.isEmpty()) this.players.remove(uuid);
            }

            if (this.players.isEmpty()) {
                unregister();
                registeredListenerMap.remove(key);
            }
        }

        public void register() {
            if(isRegistered) return;

            registry.getHandlerList(key.event).register(registeredListener);

            isRegistered = true;
        }

        public void unregister() {
            if(!isRegistered) return;

            registry.getHandlerList(key.event).unregister(registeredListener);

            isRegistered = false;
        }

        @Override
        public void execute(Listener listener, Event event) throws EventException {
            if(event instanceof PlayerEvent) {
                UUID uuid = ((PlayerEvent) event).getPlayer().getUniqueId();

                Set<RegisteredListener> rls = players.get(uuid);

                if (rls != null) {
                    for (RegisteredListener rl : rls) {
                        rl.callEvent(event);
                    }
                }
            } else {
                for (Set<RegisteredListener> rls : players.values()) {
                    for (RegisteredListener rl : rls) {
                        rl.callEvent(event);
                    }
                }
            }
        }
    }

}
