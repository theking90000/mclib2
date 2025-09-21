package be.theking90000.mclib2.platform.classpath;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class DependencyClassLoader extends URLClassLoader {

    private final ClassLoader firstNonDependencyClassLoaderParent;

    private final Set<PlatformDependency> loadedDependencies = new HashSet<>();

    private final Map<String, Set<URL>> localResources = new HashMap<>();
    private final Map<String, byte[]> byteCache = new HashMap<>();
    private final Map<String, Class<?>> classes = new HashMap<>();

    public DependencyClassLoader(ClassLoader parentLoader) {
        super(new URL[0], parentLoader);

        ClassLoader p = getParent();
        while (p instanceof DependencyClassLoader) {
            p = p.getParent();
        }
        firstNonDependencyClassLoaderParent = p;
    }

    /**
     * Adds a dependency to this classloader.
     * If the dependency is already loaded, this method returns an empty set.
     *
     * @param dependency the dependency to add
     * @return the conflicting classes that cannot be added
     */
    protected Set<String> addDependency(PlatformDependency dependency) {
        if(loadedDependencies.contains(dependency)) return Collections.emptySet();
        loadedDependencies.add(dependency);

        UnpackedDependency unpacked = dependency.unpack();

        addLocalResources(unpacked.getResources());
        Set<String> conflicts = new HashSet<>();

        ClassLoader p = getParent();
        if(p instanceof DependencyClassLoader) {
            // Attempt to register the dependency into parent
            // If the parent says 'class conflict' (already registered classes in parent)
            // register locally.
            for(String classConflict : ((DependencyClassLoader) p).addDependency(dependency)) {
                if (!addClass(classConflict, unpacked.getClasses().get(classConflict))) {
                    conflicts.add(classConflict);
                }
            }
        } else {
            for (Map.Entry<String, byte[]> entry : unpacked.getClasses().entrySet()) {
                if (!addClass(entry.getKey(), entry.getValue())) {
                    conflicts.add(entry.getKey());
                }
            }
        }

        return conflicts;
    }


    private boolean addClass(String className, byte[] bytes) {
        if(byteCache.containsKey(className)) return false;
        byteCache.put(className, bytes);
        return true;
    }

    private void addLocalResources(Map<String, Set<URL>> resources) {
        for (Map.Entry<String, Set<URL>> entry : resources.entrySet()) {
            localResources.compute(entry.getKey(), (k, v) -> {
                if (v == null) v = new HashSet<>();
                v.addAll(entry.getValue());
                return v;
            });
        }
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        return getResources(name);
    }

    @Override
    public URL findResource(String name) {
        return getResource(name);
    }

    @Override
    public URL getResource(String name) {
        try {
            return getResources(name).nextElement();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Set<URL> localRes = localResources.get(name);
        return new CompoundEnumeration<>(new Enumeration[] {
                localRes != null ? Collections.enumeration(localRes) : Collections.emptyEnumeration(),
                firstNonDependencyClassLoaderParent != null ? firstNonDependencyClassLoaderParent.getResources(name) : Collections.emptyEnumeration()
            }
        );
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> loadedClass = findLoadedClass(name);

            if (loadedClass == null) {
                try {
                    loadedClass = findLocalClassImpl(name, resolve);
                } catch (NullPointerException e) {
                }
            }

            if (loadedClass == null) {
                try {
                    loadedClass = super.findClass(name);
                } catch (ClassNotFoundException e) {
                    // class is not found in the given urls.
                    // Let's try it in parent classloader.
                    // If class is still not found, then this method will throw class not found ex.
                    try {
                        loadedClass = getParent().loadClass(name);
                    } catch (NullPointerException | ClassNotFoundException e2) {
                        loadedClass = super.loadClass(name, resolve);
                    }

                }
            }

            if (resolve) {      // marked to resolve
                resolveClass(loadedClass);
            }
            return loadedClass;
        }
    }

    private Class<?> findLocalClassImpl(String className, boolean resolve) throws ClassNotFoundException {
        return getLoadedClass(className, resolve);
    }

    private void definePackageForClass(String className) {
        int i = className.lastIndexOf('.');
        if (i != -1) {
            String pkgname = className.substring(0, i);
            // define the package if it is not already defined
            Package pkg = getPackage(pkgname);
            if (pkg == null) {
                definePackage(pkgname, null, null, null, null, null, null, null);
            }
        }
    }

    protected Class<?> getLoadedClass(String className, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(className)) {

            Class<?> loadedClass = findLoadedClass(className);
            if (classes.containsKey(className)) {
                return classes.get(className);
            }
            if (byteCache.containsKey(className)) {
                definePackageForClass(className);
                byte[] classBytes = byteCache.get(className);

                if (loadedClass == null) {
                    try {
                        loadedClass = defineClass(className, classBytes, 0, classBytes.length, this.getClass().getProtectionDomain());
                    } catch (NoClassDefFoundError | IncompatibleClassChangeError e) {
                        throw new ClassNotFoundException(className, e);
                    }
                }
                classes.put(className, loadedClass);
                if (resolve) {
                    resolveClass(loadedClass);
                }
                return loadedClass;
            } else {
                return super.findLoadedClass(className);
            }
        }
    }

    public static final class CompoundEnumeration<E> implements Enumeration<E> {
        private final Enumeration<E>[] enums;
        private int index = 0;

        public CompoundEnumeration(Enumeration<E>[] enums) {
            this.enums = enums;
        }

        @Override
        public boolean hasMoreElements() {
            while (index < enums.length) {
                if (enums[index] != null && enums[index].hasMoreElements()) {
                    return true;
                }
                index++;
            }
            return false;
        }

        @Override
        public E nextElement() {
            if (hasMoreElements()) {
                return enums[index].nextElement();
            }
            throw new NoSuchElementException();
        }
    }

}
