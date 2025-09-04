package be.theking90000.mclib2.test;

import be.theking90000.mclib2.platform.PluginDescriptor;
import be.theking90000.mclib2.platform.boot.PlatformBoot;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class TestPluginRegister {

    private PluginDescriptor descriptor(String e) throws Exception {
        PluginDescriptor pd = new PluginDescriptor(false, new HashSet<>(), new HashSet<>());

        addEntryPoint(pd.entryPoints, e);
        addDependencies(pd.dependencies);
        addSelf(pd.dependencies);

        return pd;
    }

    private void addEntryPoint(Set<String> entries, String a) {
        entries.add(a);
    }

    // Add self as dependency to test devPath loading
    private void addSelf(Set<PluginDescriptor.Dependency> deps) throws Exception {
        String[] p = System.getProperty("java.class.path").split(";");
        for (String s : p) {
            if (s.contains("classes/java/test") || s.contains("classes\\java\\test")) {
                deps.add(new PluginDescriptor.Dependency(s, "be.theking90000.mclib2:platform-boot-test:0.0.1", null, null));
            }
        }
    }

    private void addDependencies(Set<PluginDescriptor.Dependency> deps) {
        deps.add(new PluginDescriptor.Dependency(null, "org.slf4j:slf4j-api:2.0.17", "7b751d952061954d5abfed7181c1f645d336091b679891591d63329c622eb832", "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar"));
    }

    @Test
    public void testLoadPlugin() throws Exception {
        PluginDescriptor pd1 = descriptor("be.theking90000.mclib2.test.PluginEntrypoint1");
        PluginDescriptor pd2 = descriptor("be.theking90000.mclib2.test.PluginEntrypoint2");

        pd2.dependencies.add(new PluginDescriptor.Dependency(null, "org.slf4j:slf4j-simple:2.0.17", "ddfea59ac074c6d3e24ac2c38622d2d963895e17f70b38ed4bdae4d780be6964", "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.17/slf4j-simple-2.0.17.jar"));

        assert !pd1.equals(pd2);

        PlatformBoot.register(pd1, null);
        PlatformBoot.register(pd2, null);

        int loaded = PlatformBoot.boot();

        assert loaded == 3;

        PlatformBoot.unregister(pd1);
        PlatformBoot.unregister(pd2);

        PlatformBoot.shutdown();
    }

    @Test
    public void testConflictingDeps() throws Exception {
        PluginDescriptor pd1 = descriptor("be.theking90000.mclib2.test.PluginEntrypoint1");
        PluginDescriptor pd2 = descriptor("be.theking90000.mclib2.test.PluginEntrypoint2");
        PluginDescriptor pd3 = descriptor("be.theking90000.mclib2.test.PluginEntrypoint3");

        pd2.dependencies.add(new PluginDescriptor.Dependency(null, "org.slf4j:slf4j-simple:2.0.17", "ddfea59ac074c6d3e24ac2c38622d2d963895e17f70b38ed4bdae4d780be6964", "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.17/slf4j-simple-2.0.17.jar"));
        pd3.dependencies.clear();
        addSelf(pd3.dependencies);
        pd1.dependencies.add(new PluginDescriptor.Dependency(null, "org.slf4j:slf4j-simple:1.0", "033d70f0b2d1a51b1898614bb2cc9e7a60239328f17f08731bfbf9e15f068200", "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.0/slf4j-simple-1.0.jar"));
        pd3.dependencies.add(new PluginDescriptor.Dependency(null, "org.slf4j:slf4j-simple:1.0", "033d70f0b2d1a51b1898614bb2cc9e7a60239328f17f08731bfbf9e15f068200", "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.0/slf4j-simple-1.0.jar"));
        pd3.dependencies.add(new PluginDescriptor.Dependency(null, "org.slf4j:slf4j-api:2.0.16", "a12578dde1ba00bd9b816d388a0b879928d00bab3c83c240f7013bf4196c579a", "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar"));

        PlatformBoot.register(pd1, null);
        PlatformBoot.register(pd2, null);
        PlatformBoot.register(pd3, null);

        int loaded = PlatformBoot.boot();

        assert loaded == 5;

        // in this context the class loaded is not the one in the test classes but the one in the jar
        // This proves child-first loading works.
        assert PluginEntrypoint1.init == 0;

        PlatformBoot.unregister(pd1);
        PlatformBoot.unregister(pd2);
        PlatformBoot.unregister(pd3);

        PlatformBoot.shutdown();
    }

}
