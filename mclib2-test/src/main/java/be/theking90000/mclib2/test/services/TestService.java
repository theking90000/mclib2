package be.theking90000.mclib2.test.services;

import be.theking90000.mclib2.integration.Service;
import jakarta.inject.Inject;
import org.bukkit.plugin.java.JavaPlugin;

@Service
public class TestService {

    private final JavaPlugin plugin;

    @Inject
    public TestService(JavaPlugin plugin) {
        this.plugin = plugin;

        test();
    }

    public void test() {
        plugin.getLogger().info("TestService called!");
    }

}
