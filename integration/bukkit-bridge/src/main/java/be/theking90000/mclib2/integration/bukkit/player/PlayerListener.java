package be.theking90000.mclib2.integration.bukkit.player;

import be.theking90000.mclib2.integration.bukkit.BukkitListener;
import com.google.inject.Inject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@BukkitListener
public class PlayerListener implements Listener {

    private final PlayerScope playerScope;

    @Inject
    public PlayerListener(PlayerScope playerScope) {
        this.playerScope = playerScope;
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        try {
            playerScope.enter(player.getUniqueId());
            playerScope.seed(Player.class, player);

            // Initialize player-scoped Services, Listener, etc.
            // Maybe use an init(Scope) GLOBAL method or interface
            // And when a new Scope is entered (not necessarily a PlayerScope, but any Scope)
            // the Injector calls init(Scope) every where it can.

        } finally {
            playerScope.remove(player.getUniqueId());
            playerScope.exit();
        }
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        try {
            playerScope.enter(player.getUniqueId());

        } finally {
            playerScope.remove(player.getUniqueId());
            playerScope.exit();
        }
    }

}
