package be.theking90000.mclib2.config.bukkit;

import be.theking90000.mclib2.config.ConfigResolver;
import com.google.inject.Inject;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;

public class BukkitConfigResolver implements ConfigResolver {

    private final JavaPlugin javaPlugin;

    @Inject
    public BukkitConfigResolver(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    private File getFile(String name) {
        File folder = javaPlugin.getDataFolder();
        if(!folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder, name);
    }

    @Override
    public InputStream resolve(String name) throws IOException  {
        File file = getFile(name);

        return Files.newInputStream(file.toPath());
    }

    @Override
    public OutputStream store(String name) throws IOException {
        File file = getFile(name);

        return Files.newOutputStream(file.toPath());
    }
}
