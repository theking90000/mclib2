package be.theking90000.mclib2.integration.bukkit.listener;

import be.theking90000.mclib2.integration.bukkit.player.PlayerScope;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@Singleton
public class BukkitPlayerListener implements Listener {

    private final Map<Player, Map<Class<? extends Event>, Set<RegisteredListener>>> playerListeners = new HashMap<>();
   // private final Map<Class<? extends Listener>, Map<Class<? extends Event>, Set<RegisteredListener>>> listenerClasses = new HashMap<>();
    private final Map<Class<? extends Listener>, List<PlayerRegisteredListener>> listenerClasses = new HashMap<>();

    private final PlayerScope playerScope;
    private final JavaPlugin javaPlugin;
    private final Injector injector;
    private final BukkitListenerManager bukkitListenerManager;

    @Inject
    public BukkitPlayerListener(PlayerScope playerScope, JavaPlugin javaPlugin, BukkitListenerManager bukkitListenerManager, Injector injector) {
        this.playerScope = playerScope;
        this.javaPlugin = javaPlugin;
        this.bukkitListenerManager = bukkitListenerManager;
        this.injector = injector;
    }

    public void addListenerClass(Class<? extends Listener> listenerClass) {
        if(!listenerClasses.containsKey(listenerClass)) {
            listenerClasses.put(listenerClass, new ArrayList<>());

            for (Player player: javaPlugin.getServer().getOnlinePlayers()) {
                playerScope.enter(player.getUniqueId());
                try {
                    playerScope.seed(Player.class, player);

                    registerListenerForPlayer(player, injector.getInstance(listenerClass));
                } finally {
                    playerScope.exit();
                }
            }
        }
    }

    public void removeListenerClass(Class<? extends Listener> listenerClass) {
        if(listenerClasses.containsKey(listenerClass)) {
            for (Player player: javaPlugin.getServer().getOnlinePlayers()) {
                unregisterListenerForPlayer(player, listenerClass);
            }

            for (PlayerRegisteredListener prl : listenerClasses.get(listenerClass)) {
                bukkitListenerManager.unregisterListener(prl.getEvent(), prl);
            }
            listenerClasses.remove(listenerClass);
        }
    }

    public void removeAllListenerClass() {
        for (Class<? extends Listener> listenerClass : new HashSet<>(listenerClasses.keySet())) {
            removeListenerClass(listenerClass);
        }
    }

    private Map<Class<? extends Event>, Set<RegisteredListener>> getRegisteredListenersForClass(Class<? extends Listener> listenerClass, Listener instance) {
        if(!listenerClasses.containsKey(listenerClass)) {
            throw new IllegalArgumentException("Listener class " + listenerClass.getName() + " is not registered");
        }

        return javaPlugin.getPluginLoader().createRegisteredListeners(instance, javaPlugin);
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        playerScope.enter(player.getUniqueId());

        try {
            playerScope.seed(Player.class, player);

            for (Class<? extends Listener> listenerClass : listenerClasses.keySet()) {
                registerListenerForPlayer(player, injector.getInstance(listenerClass));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            playerScope.exit();
        }
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        playerScope.enter(player.getUniqueId());

        try {
            unregisterListenerForPlayer(player);
        } finally {
            playerScope.remove(player.getUniqueId());
            playerScope.exit();
        }
    }

    private Listener registerListenerForPlayer(Player player, Listener listener) {
        Class<? extends Listener> listenerClass = listener.getClass();

        List<PlayerRegisteredListener> playerRegisteredListeners = this.listenerClasses.get(listenerClass);

        int i = 0;
        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : getRegisteredListenersForClass(listenerClass, listener).entrySet()) {
            for (RegisteredListener registeredListener : entry.getValue()) {
                if(PlayerEvent.class.isAssignableFrom(entry.getKey())) {

                    PlayerRegisteredListener prl;

                    if (i >= playerRegisteredListeners.size()) {
                        prl = new PlayerRegisteredListener(registeredListener, entry.getKey());
                        bukkitListenerManager.registerListener(prl.getEvent(), prl);
                        playerRegisteredListeners.add(prl);
                    } else {
                        prl = playerRegisteredListeners.get(i);
                    }

                    prl.addPlayerListener(player, registeredListener);
                    registerListenerForPlayer(player, entry.getKey(), registeredListener);

                    i+=1;
                } else {
                    bukkitListenerManager.registerListener(entry.getKey(), registeredListener);
                    registerListenerForPlayer(player, entry.getKey(), registeredListener);
                }
            }
        }

        return listener;
    }

    private void registerListenerForPlayer(Player player, Class<? extends Event> event, RegisteredListener l) {
        playerListeners.computeIfAbsent(player, (k) -> new HashMap<>())
                .computeIfAbsent(event, (k) -> new HashSet<>())
                .add(l);
    }

    private void unregisterListenerForPlayer(Player player) {
        Map<Class<? extends Event>, Set<RegisteredListener>> listeners = playerListeners.remove(player);
        if (listeners != null) {

            for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : listeners.entrySet()) {
                for (RegisteredListener rl : entry.getValue()) {

                    List<PlayerRegisteredListener> playerRegisteredListeners = this.listenerClasses.get(rl.getListener().getClass());

                    if(PlayerEvent.class.isAssignableFrom(entry.getKey())) {
                        for (PlayerRegisteredListener prl : playerRegisteredListeners) {
                            prl.removePlayerListener(player, rl);
                        }
                    } else {
                        bukkitListenerManager.unregisterListener(entry.getKey(), rl);
                    }
                }
            }
        }
    }

    private void unregisterListenerForPlayer(Player player, Class<? extends Listener> listenerClass) {
        Map<Class<? extends Event>, Set<RegisteredListener>> listeners = playerListeners.get(player);
        if (listeners != null) {
            for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : new HashSet<>(listeners.entrySet())) {
                for (RegisteredListener rl : entry.getValue()) {

                    if(rl.getListener().getClass().equals(listenerClass)) {
                        List<PlayerRegisteredListener> playerRegisteredListeners = this.listenerClasses.get(rl.getListener().getClass());

                        if(PlayerEvent.class.isAssignableFrom(entry.getKey())) {
                            for (PlayerRegisteredListener prl : playerRegisteredListeners) {
                                prl.removePlayerListener(player, rl);
                            }
                        } else {
                            bukkitListenerManager.unregisterListener(entry.getKey(), rl);
                        }
                        listeners.get(entry.getKey()).remove(rl);
                        if (listeners.get(entry.getKey()).isEmpty()) {
                            listeners.remove(entry.getKey());
                        }
                    }
                }
            }

            if (listeners.isEmpty()) {
                playerListeners.remove(player);
            }
        }
    }


}
