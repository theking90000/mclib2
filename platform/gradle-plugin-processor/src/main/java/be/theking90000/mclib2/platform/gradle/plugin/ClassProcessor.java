package be.theking90000.mclib2.platform.gradle.plugin;

import java.nio.file.Path;

public interface ClassProcessor {
    void processClass(Class<?> clazz, Path outputDir);
}
