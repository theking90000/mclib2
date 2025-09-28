package be.theking90000.mclib2.test.services;

import be.theking90000.mclib2.integration.Service;
import be.theking90000.mclib2.test.config.MainConfig;
import jakarta.inject.Inject;
import org.bukkit.plugin.java.JavaPlugin;

@Service
public class TestService {

    private final JavaPlugin plugin;
    private final MainConfig mainConfig;

    @Inject
    public TestService(JavaPlugin plugin, MainConfig mainConfig) {
        this.plugin = plugin;
        this.mainConfig = mainConfig;

        test();
    }

    public void test() {
        plugin.getLogger().info("TestService called!");
    }

}
