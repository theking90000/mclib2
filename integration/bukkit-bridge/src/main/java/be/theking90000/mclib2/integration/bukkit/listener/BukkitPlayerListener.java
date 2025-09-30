package be.theking90000.mclib2.integration.bukkit.listener;

import be.theking90000.mclib2.integration.bukkit.player.PlayerScope;
import com.google.inject.Injector;
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

public class BukkitPlayerListener implements Listener {

    private final Map<Player, Map<Class<? extends Event>, Set<RegisteredListener>>> playerListeners = new HashMap<>();
   // private final Map<Class<? extends Listener>, Map<Class<? extends Event>, Set<RegisteredListener>>> listenerClasses = new HashMap<>();
    private final Set<Class<? extends Listener>> listenerClasses = new HashSet<>();

    private final PlayerScope playerScope;
    private final JavaPlugin javaPlugin;
    private final Injector injector;
    private final BukkitListenerManager bukkitListenerManager;

    public BukkitPlayerListener(PlayerScope playerScope, JavaPlugin javaPlugin, BukkitListenerManager bukkitListenerManager, Injector injector) {
        this.playerScope = playerScope;
        this.javaPlugin = javaPlugin;
        this.bukkitListenerManager = bukkitListenerManager;
        this.injector = injector;
    }

    public void addListenerClass(Class<? extends Listener> listenerClass) {
        if(!listenerClasses.contains(listenerClass)) {
            listenerClasses.add(listenerClass);

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

    private Map<Class<? extends Event>, Set<RegisteredListener>> getRegisteredListenersForClass(Class<? extends Listener> listenerClass, Listener instance) {
        if(!listenerClasses.contains(listenerClass)) {
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

            for (Class<? extends Listener> listenerClass : listenerClasses) {
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

        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : getRegisteredListenersForClass(listenerClass, listener).entrySet()) {
            for (RegisteredListener registeredListener : entry.getValue()) {
                registerListenerForPlayer(player, entry.getKey(), registeredListener, listener);
            }
        }

        return listener;
    }

    private void registerListenerForPlayer(Player player, Class<? extends Event> event, RegisteredListener registeredListener, Listener listener) {
        RegisteredListener l = registeredListener;

        if (PlayerEvent.class.isAssignableFrom(event)) {
            l = new PlayerRegisteredListener(registeredListener, player, listener);
        }

        bukkitListenerManager.registerListener(event, l);

        playerListeners.computeIfAbsent(player, (k) -> new HashMap<>())
                .computeIfAbsent(event, (k) -> new HashSet<>())
                .add(l);
    }

    private void unregisterListenerForPlayer(Player player) {
        Map<Class<? extends Event>, Set<RegisteredListener>> listeners = playerListeners.remove(player);
        if (listeners != null) {
            for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : listeners.entrySet()) {
                for (RegisteredListener rl : entry.getValue()) {
                    bukkitListenerManager.unregisterListener(entry.getKey(), rl);
                }
            }
        }

    }


}
