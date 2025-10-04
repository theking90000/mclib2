package be.theking90000.mclib2.integration.bukkit.player;

import be.theking90000.mclib2.inject.Disposable;
import be.theking90000.mclib2.integration.bukkit.BukkitListener;
import be.theking90000.mclib2.integration.scope.ScopeManager;
import com.google.inject.Inject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

@BukkitListener
public class PlayerListener implements Listener, Disposable {

    private final PlayerScope playerScope;
    private final ScopeManager scopeManager;
    private final JavaPlugin plugin;

    @Inject
    public PlayerListener(PlayerScope playerScope, ScopeManager scopeManager, JavaPlugin plugin) {
        this.playerScope = playerScope;
        this.scopeManager = scopeManager;
        this.plugin = plugin;

        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                // Create next tick
                for (Player player : players) {
                    if(player.isOnline())
                        createScope(player);
                }
            }
        });
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        createScope(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        removeScope(event.getPlayer());
    }

    private void createScope(Player player) {
        try {
            playerScope.enter(player.getUniqueId());
            playerScope.seed(Player.class, player);

            scopeManager.notifyScopeCreation(playerScope);
        } finally {
            playerScope.remove(player.getUniqueId());
            playerScope.exit();
        }
    }

    private void removeScope(Player player) {
        try {
            playerScope.enter(player.getUniqueId());

            scopeManager.notifyScopeDeletion(playerScope);
        } finally {
            playerScope.remove(player.getUniqueId());
            playerScope.exit();
        }
    }

    @Override
    public void dispose() {
        for(Player p : plugin.getServer().getOnlinePlayers()) {
            removeScope(p);
        }
    }
}
