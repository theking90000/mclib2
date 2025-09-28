package be.theking90000.mclib2.integration.bukkit.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.RegisteredListener;

public class PlayerRegisteredListener extends RegisteredListener {

    private RegisteredListener registeredListener;
    private Player player;

    public PlayerRegisteredListener(RegisteredListener registeredListener, Player player, Listener listener) {
        super(listener, null, registeredListener.getPriority(), registeredListener.getPlugin(), registeredListener.isIgnoringCancelled());
        this.registeredListener = registeredListener;
        this.player = player;
    }

    @Override
    public void callEvent(Event event) throws EventException {
       if(((PlayerEvent) event).getPlayer() == player) {
           registeredListener.callEvent(event);
       }
    }
}
