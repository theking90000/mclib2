package be.theking90000.mclib2.integration.bukkit.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.*;

@Singleton
public class BukkitListenerRegistry {

    private final Set<BukkitListenerEntry> entries = new HashSet<>();
    private final Map<Listener, BukkitListenerEntry> entryMap = new HashMap<>();

    private final JavaPlugin plugin;
    private final HandlerListCache handlerList;

    @Inject
    public BukkitListenerRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.handlerList = new HandlerListCache(plugin.getServer().getPluginManager());
    }

    public BukkitListenerEntry createEntry(Listener lst) {
        BukkitListenerEntry entry = BukkitListenerEntry.create(this, lst);

        entries.add(entry);
        entryMap.put(lst, entry);

        return entry;
    }

    public BukkitListenerEntry removeEntry(Listener lst) {
        BukkitListenerEntry entry = entryMap.remove(lst);
        if(entry != null) {
            entries.remove(entry);
        }
        return entry;
    }

    protected JavaPlugin getPlugin() {
        return this.plugin;
    }

    protected HandlerList getHandlerList(Class<? extends Event> eventClass) {
        return handlerList.getHandlerList(eventClass);
    }

    public static class HandlerListCache {
        private final Map<Class<? extends Event>, HandlerList> handlerLists = new java.util.HashMap<>();
        private final PluginManager pluginManager;

        public HandlerListCache(PluginManager pluginManager) {
            this.pluginManager = pluginManager;
        }

        public HandlerList getHandlerList(Class<? extends Event> eventClass) {
            return handlerLists.computeIfAbsent(eventClass, this::getHandlerList0);
        }

        public HandlerList getHandlerList0(Class<? extends Event> eventClass) {
            try {
                Method method = pluginManager.getClass().getDeclaredMethod("getEventListeners", Class.class);
                method.setAccessible(true);
                return (HandlerList) method.invoke(pluginManager, eventClass);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("Failed to get HandlerList for event " + eventClass.getCanonicalName(), ex);
            }
        }

    }

}
