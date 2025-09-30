package be.theking90000.mclib2.test;

import be.theking90000.mclib2.integration.bukkit.BukkitListener;
import be.theking90000.mclib2.integration.bukkit.PlayerScoped;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

@BukkitListener
@PlayerScoped
public class PlayerHitListener implements Listener {

    private final CustomPlayer player;

    @Inject
    public PlayerHitListener(CustomPlayer player) {
        this.player = player;
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        player.addMoney(1);
        player.getPlayer().sendMessage("You interacted with " + event.getAction().name() + " | Money: " + player.getMoney());
    }

}
