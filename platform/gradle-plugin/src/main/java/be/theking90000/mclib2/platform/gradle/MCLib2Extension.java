package be.theking90000.mclib2.platform.gradle;


import org.gradle.api.provider.Property;

public abstract class MCLib2Extension {

    public MCLib2Extension() {
        getVersion().convention(GradlePlugin.getVersion());
        getDisableFramework().convention(false);
    }

    public abstract Property<String> getVersion();

    public abstract Property<Boolean> getDisableFramework();
}
