package be.theking90000.mclib2.test;

import be.theking90000.mclib2.integration.bukkit.BukkitListener;
import be.theking90000.mclib2.integration.bukkit.PlayerScoped;
import com.google.inject.Inject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

@BukkitListener
@PlayerScoped
public class PlayerHitListener implements Listener {

    private final Player player;

    @Inject
    public PlayerHitListener(Player player) {
        this.player = player;
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        player.sendMessage("You interacted with " + event.getAction().name());
    }

}
