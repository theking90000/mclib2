package be.theking90000.mclib2.test;

import be.theking90000.mclib2.integration.bukkit.PlayerScoped;
import com.google.inject.Inject;
import org.bukkit.entity.Player;

@PlayerScoped
public class CustomPlayer {

    private final Player player;

    private int money = 0;

    @Inject
    public CustomPlayer(Player player) {
        this.player = player;
    }

    public int getMoney() {
        return money;
    }

    public void addMoney(int amount) {
        this.money += amount;
    }

    public Player getPlayer() {
        return player;
    }
}
