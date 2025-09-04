package be.theking90000.mclib2.platform.classpath;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class ChildFirstClassLoader extends URLClassLoader implements ClasspathAppender {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public ChildFirstClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    @Override
    public void appendFileToClasspath(Path path) throws MalformedURLException {
        addURL(path.toUri().toURL());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass == null) {
                try {
                    loadedClass = findClass(name);
                } catch (ClassNotFoundException e) {
                    // class is not found in the given urls.
                    // Let's try it in parent classloader.
                    // If class is still not found, then this method will throw class not found ex.
                    loadedClass = super.loadClass(name, resolve);
                }
            }

            if (resolve) {      // marked to resolve
                resolveClass(loadedClass);
            }
            return loadedClass;
        }
    }


}
