package be.theking90000.mclib2.test;

import be.theking90000.mclib2.platform.PluginDescriptor;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

public class TestDescriptor {

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

    private void addSelf(Set<PluginDescriptor.Dependency> deps) throws Exception {
        String pp = System.getProperty("java.class.path");
        String[] p = pp.split(pp.contains(";")? ";":":");
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
    void testDescriptorSerialization() throws Exception {
        PluginDescriptor pd = descriptor("be.theking90000.mclib2.test.TestService");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        pd.serialize(outputStream);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        PluginDescriptor pd2 = PluginDescriptor.deserialize(inputStream);

        assert pd.equals(pd2);
    }

}
