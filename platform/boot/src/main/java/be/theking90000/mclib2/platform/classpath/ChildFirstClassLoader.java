package be.theking90000.mclib2.platform.classpath;

import java.net.URL;

public class ChildFirstClassLoader extends NestedURLClassLoader /*URLClassLoader*/ implements ClasspathAppender {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public ChildFirstClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public void appendUrlToClasspath(URL url) {
        addURL(url);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }


}
