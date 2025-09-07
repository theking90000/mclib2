package be.theking90000.mclib2.test;

import be.theking90000.mclib2.platform.boot.PlatformRegistry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class TestPlugin {

    @Test
    public void testLoadPlugin() throws Exception {
        String pp = System.getProperty("java.class.path");
        String[] p = pp.split(pp.contains(";")? ";":":");

        URL[] urls = new URL[p.length];
        for (int i = 0; i < p.length; i++) {
            boolean isJar = p[i].endsWith(".jar");
            urls[i] = new URL("file://" + p[i] + (isJar ? "" : "/"));
        }

        ClassLoader cl1 = new URLClassLoader(urls, null);
        ClassLoader cl2 = new URLClassLoader(urls, null);
        Class<?> s1 = cl1.loadClass("be.theking90000.mclib2.platform.boot.PlatformSingleton");
        Class<?> s2 = cl2.loadClass("be.theking90000.mclib2.platform.boot.PlatformSingleton");

        assert s1 != s2; // Different class loaders, different classes

        // But still manages to get the same singleton instance via SystemClassLoader and reflection
        assert getRegistry(s1) == getRegistry(s2);

        assert getRegistry(s1).getClass() == getRegistry(s2).getClass();
    }

    private PlatformRegistry getRegistry(Class<?> singletonClass) throws Exception {
        Method m = singletonClass.getDeclaredMethod("getRegistry");
        m.setAccessible(true);
        // Not sure this cast will work
        return (PlatformRegistry) m.invoke(null);
    }

}
