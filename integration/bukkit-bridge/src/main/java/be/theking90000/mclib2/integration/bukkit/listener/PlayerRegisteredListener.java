package be.theking90000.mclib2.integration.bukkit.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.RegisteredListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerRegisteredListener extends RegisteredListener {

    private final Map<Player, Set<RegisteredListener>> playerRegisteredListeners = new HashMap<>();
    private final Class<? extends Event> event;

    protected PlayerRegisteredListener(RegisteredListener registeredListener, Class<? extends Event> event) {
        super(/* dummy listener*/new Listener() {},
                null,
                registeredListener.getPriority(),
                registeredListener.getPlugin(),
                registeredListener.isIgnoringCancelled());
        this.event = event;
    }

    public Class<? extends Event> getEvent() {
        return event;
    }

    protected void addPlayerListener(Player player, RegisteredListener listeners) {
        playerRegisteredListeners.computeIfAbsent(player, (k) -> new HashSet<>()).add(listeners);
    }

    protected void removePlayerListener(Player player, RegisteredListener listeners) {
        Set<RegisteredListener> set = playerRegisteredListeners.get(player);
        if(set != null) {
            set.remove(listeners);
            if(set.isEmpty()) {
                playerRegisteredListeners.remove(player);
            }
        }
    }

    @Override
    public void callEvent(Event event) throws EventException {
       Player p = ((PlayerEvent) event).getPlayer();
       Set<RegisteredListener> registeredListeners = playerRegisteredListeners.get(p);
       if (registeredListeners != null) {
           for (RegisteredListener rl : registeredListeners) {
               rl.callEvent(event);
           }
       }
    }
}
