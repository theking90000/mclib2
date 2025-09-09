package be.theking90000.mclib2.platform.classpath;

import be.theking90000.mclib2.platform.PluginDescriptor;
import be.theking90000.mclib2.platform.RegisteredPlugin;

import java.util.*;

public class PlatformClasspath {

    private final ChildFirstClassLoader platformClassLoader;

    private final Set<String> globalIdsLoaded = new HashSet<>();
    private final Set<PlatformDependency> globalDependencies = new HashSet<>();
    private final Map<RegisteredPlugin<?>, Set<PlatformDependency>> localDependencies = new HashMap<>();

    private final Map<ClasspathEntry, Integer> awaitingDependencies = new HashMap<>();
    private final Map<ClasspathEntry, PlatformDependency> platformDependencies = new HashMap<>();

    public PlatformClasspath() {
        platformClassLoader = new ChildFirstClassLoader(getClass().getClassLoader());
    }

    public ChildFirstClassLoader createPluginLoader() {
        return new ChildFirstClassLoader(platformClassLoader);
    }


    /**
     * Registers the dependencies of a plugin.
     * This does not load the dependencies, it only registers them.
     *
     * @param registeredPlugin the plugin whose dependencies to register
     */
    public void registerDependencies(RegisteredPlugin<?> registeredPlugin) {
        for (ClasspathEntry dependency : registeredPlugin.getDependencies()) {
            platformDependencies.computeIfAbsent(dependency, (k) -> new PlatformDependency(dependency, registeredPlugin.callerClassLoader));
            awaitingDependencies.compute(dependency, (k, v) -> v == null ? 1 : v + 1);
        }
    }

    /**
     * Loads the dependencies of a plugin and adds them to the classpath.
     *
     * @param registeredPlugin the plugin whose dependencies to load
     * @return the number of dependencies that were added to the local classpath
     */
    public int loadPluginDependencies(RegisteredPlugin<?> registeredPlugin) {
        int loaded = this.processAwaitingDependencies();

        for (ClasspathEntry dependency : registeredPlugin.getDependencies()) {
            if (loadDependency(dependency, registeredPlugin)) loaded++;
        }

        return loaded;
    }

    public void unregisterDependencies(RegisteredPlugin<?> registeredPlugin) {
        localDependencies.remove(registeredPlugin);

        for (ClasspathEntry dep : registeredPlugin.getDependencies()) {
            // Decrease the count of awaiting dependencies
            awaitingDependencies.computeIfPresent(dep, (k, v) -> v > 1 ? v - 1 : null);
        }

        try {
            registeredPlugin.classLoader.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to close plugin classloader", e);
        }

        // We drop all references to registeredPlugin, but we do not unload global dependencies
        // because unload global dependencies cannot be done without recreating the classloader
    }

    public void shutdown() {
        globalIdsLoaded.clear();

        for (RegisteredPlugin<?> r : localDependencies.keySet()) {
            try {
                r.classLoader.close();
            } catch (Exception e) {
                throw new RuntimeException("Failed to close plugin classloader", e);
            }
        }

        localDependencies.clear();
        globalDependencies.clear();
        awaitingDependencies.clear();
        platformDependencies.clear();

        try {
            platformClassLoader.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to close platform classloader", e);
        }
    }

    /**
     * Loads a dependency for a plugin, either as a global dependency or as a local dependency.
     * The dependency is loaded as a global dependency if it is not already loaded and if there are no conflicts
     * with other global dependencies (same maven coordinates but different sha256 or version).
     *
     * @param dependency the dependency to load
     * @param plugin     the plugin requesting the dependency
     * @return true if the dependency was added to the classpath, false if was already loaded
     */
    private boolean loadDependency(ClasspathEntry dependency, RegisteredPlugin<?> plugin) {
        PlatformDependency pDep = platformDependencies.get(dependency);

        if (pDep == null) throw new RuntimeException("Dependency not registered: " + dependency);

        // Already loaded as a global dependency, nothing to do.
        // Is accessible from "platformClassLoader"
        if (globalDependencies.contains(pDep)) return false;

        // Check if we have a dependency conflict (same global identifier but different unique ID)
        // If this is the case, the dependency cannot be loaded as a global dependency.
        // and must be loaded as a local dependency.
        if (globalIdsLoaded.contains(dependency.getGlobalID())) {
            Set<PlatformDependency> localDeps = localDependencies.computeIfAbsent(plugin, (k) -> new HashSet<>());
            if (localDeps.contains(pDep)) return false;

            pDep.load(plugin.classLoader);
            localDeps.add(pDep);
            return true;
        }

        // Load as a global dependency
        pDep.load(platformClassLoader);
        globalDependencies.add(pDep);
        globalIdsLoaded.add(dependency.getGlobalID());
        return true;
    }

    private int processAwaitingDependencies() {
        if (awaitingDependencies.isEmpty()) return 0;

        int loaded = 0;

        List<Map.Entry<ClasspathEntry, Integer>> sorted =
                new ArrayList<>(awaitingDependencies.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (Map.Entry<ClasspathEntry, Integer> entry : sorted) {
            ClasspathEntry dependency = entry.getKey();
            PlatformDependency pDep = platformDependencies.get(dependency);

            if (pDep == null) throw new RuntimeException("Dependency not registered: " + dependency);

            // Attempt to load the dependency as a global dependency
            if (!globalDependencies.contains(pDep) && !globalIdsLoaded.contains(dependency.getGlobalID())) {
                pDep.load(platformClassLoader);
                globalDependencies.add(pDep);
                globalIdsLoaded.add(dependency.getGlobalID());
                loaded++;
            }

            // Remove from awaiting dependencies
            awaitingDependencies.remove(dependency);
        }

        return loaded;
    }

}
