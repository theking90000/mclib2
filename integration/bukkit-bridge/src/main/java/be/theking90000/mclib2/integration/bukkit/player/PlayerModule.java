package be.theking90000.mclib2.integration.bukkit.player;

import be.theking90000.mclib2.integration.guice.GuiceModule;
import be.theking90000.mclib2.integration.bukkit.PlayerScoped;
import com.google.inject.AbstractModule;
import org.bukkit.entity.Player;

@GuiceModule
public class PlayerModule extends AbstractModule {

    private final PlayerScope playerScope = new PlayerScope();

    @Override
    protected void configure() {
        requestInjection(playerScope);

        bindScope(PlayerScoped.class, playerScope);
        bind(PlayerScope.class).toInstance(playerScope);

        bind(Player.class)
                .toProvider(PlayerScope.seededKeyProvider())
                .in(PlayerScoped.class);
    }
}
