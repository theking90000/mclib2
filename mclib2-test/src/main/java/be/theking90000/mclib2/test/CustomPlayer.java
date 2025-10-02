package be.theking90000.mclib2.test;

import be.theking90000.mclib2.inject.Disposable;
import be.theking90000.mclib2.integration.bukkit.PlayerScoped;
import com.google.inject.Inject;
import org.bukkit.entity.Player;

@PlayerScoped
public class CustomPlayer implements Disposable {

    private final Player player;

    private int money = 0;

    @Inject
    public CustomPlayer(Player player) {
        System.out.println("CustomPlayer::new(\""+player.getUniqueId()+"\", \""+player.getName()+"\")");
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

    @Override
    public void dispose() {
        System.out.println("Saving player data for " + player.getName() + " | Money: " + money);
    }
}
