package be.theking90000.mclib2.integration.bukkit.listener;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BukkitListenerManager {

    private final JavaPlugin javaPlugin;
    private final PluginManager pluginManager;

    private final Map<Class<? extends Event>, HandlerList> handlerLists = new java.util.HashMap<>();
    private final Map<Class<? extends Event>, Set<RegisteredListener>> registeredListeners = new java.util.HashMap<>();
    private final Map<Listener, Map<Class<? extends Event>, Set<RegisteredListener>>> listenerMap = new java.util.HashMap<>();

    public BukkitListenerManager(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        this.pluginManager = javaPlugin.getServer().getPluginManager();
    }

    public void registerListener(Listener listener) {
        Map<Class<? extends Event>, Set<RegisteredListener>> registerListener = javaPlugin.getPluginLoader().createRegisteredListeners(listener, javaPlugin);

        if (listenerMap.containsKey(listener))
            throw new IllegalArgumentException("Listener " + listener.getClass().getName() + " is already registered");

        listenerMap.put(listener, registerListener);

        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : registerListener.entrySet()) {
            getHandlerList(entry.getKey()).registerAll(entry.getValue());
            registerListener.computeIfAbsent(entry.getKey(), (k) -> new HashSet<>()).addAll(entry.getValue());
        }
    }

    public void registerListener(Class<? extends Event> event, RegisteredListener registeredListener) {
        getHandlerList(event).register(registeredListener);
        registeredListeners.computeIfAbsent(event, (k) -> new HashSet<>()).add(registeredListener);
    }

    public void unregisterListener(Class<? extends Event> event, RegisteredListener registeredListener) {
        HandlerList handlerList = getHandlerList(event);
        handlerList.unregister(registeredListener);
        Set<RegisteredListener> set = registeredListeners.get(event);
        if (set != null) {
            set.remove(registeredListener);
            if (set.isEmpty()) {
                registeredListeners.remove(event);
            }
        }
    }

    public void unregisterAllListeners() {
        for (Listener listener : new HashSet<>(listenerMap.keySet())) {
            unregisterListener(listener);
        }
        for (Class<? extends Event> event : new HashSet<>(registeredListeners.keySet())) {
            for (RegisteredListener rl : new HashSet<>(registeredListeners.get(event))) {
                unregisterListener(event, rl);
            }
        }
        listenerMap.clear();
        registeredListeners.clear();
        handlerLists.clear();
    }

    public void unregisterListener(Listener listener) {
        Map<Class<? extends Event>, Set<RegisteredListener>> registered = listenerMap.remove(listener);
        if (registered == null)
            throw new IllegalArgumentException("Listener " + listener.getClass().getName() + " is not registered");

        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : registered.entrySet()) {
            HandlerList handlerList = getHandlerList(entry.getKey());
            for (RegisteredListener rl : entry.getValue()) {
                handlerList.unregister(rl);
            }

            Set<RegisteredListener> set = registeredListeners.get(entry.getKey());
            if (set != null) {
                set.removeAll(entry.getValue());
                if (set.isEmpty()) {
                    registeredListeners.remove(entry.getKey());
                }
            }
        }
    }

    private HandlerList getHandlerList(Class<? extends Event> eventClass) {
        return handlerLists.computeIfAbsent(eventClass, this::getHandlerList0);
    }

    private HandlerList getHandlerList0(Class<? extends Event> eventClass) {
        try {
            Method method = pluginManager.getClass().getDeclaredMethod("getEventListeners", Class.class);
            method.setAccessible(true);
            return (HandlerList) method.invoke(pluginManager, eventClass);
        } catch (ReflectiveOperationException ex) {
            javaPlugin.getLogger().warning("Cannot find HandlerList for event " + eventClass.getCanonicalName());
        }

        throw new IllegalStateException("Cannot find HandlerList for event " + eventClass.getCanonicalName());
    }

}
