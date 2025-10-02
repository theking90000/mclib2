package be.theking90000.mclib2.test;

import be.theking90000.mclib2.integration.bukkit.BukkitListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

@BukkitListener
public class ServerListPingListener implements Listener {

    static int pingCount = 0;

    @EventHandler
    public void on(ServerListPingEvent event) {
        event.setMotd("Welcome to the Test Server! | pings=" + (++pingCount));
    }

}
