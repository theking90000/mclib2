# Annotation Framework Usage

The annotation framework is the heart of MCLIB2, providing compile-time annotation processing and runtime service discovery with dependency injection capabilities.

## Dependencies Setup

To use the annotation framework, add the MCLIB2 Gradle plugin and configure the framework dependencies in your `build.gradle`:

```gradle
plugins {
    id 'java'
    id 'mclib2-plugin'
}

mclib2 {
    version = "0.0.1" // or "project" to use your project version
    // disableFramework = false  // Default: framework is enabled
}

repositories {
    mavenCentral()
    maven {
        name = "spigot"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

dependencies {
    // For Bukkit plugins
    compileOnly 'org.bukkit:bukkit:1.8.8-R0.1-SNAPSHOT'
    
    // Annotation processor (automatically added by mclib2-plugin)
    // annotationProcessor 'be.theking90000.mclib2:core-annotation-system-processor'
}
```

The `mclib2 {}` extension block automatically adds:
- `be.theking90000.mclib2:core-annotation-system-api` (implementation)
- `be.theking90000.mclib2:core-runtime` (implementation)
- `be.theking90000.mclib2:platform-boot` (compileOnly)
- Annotation processor for compile-time metadata generation

## Platform Entrypoint with Annotation Bootstrap

When using the annotation framework, your `@PlatformEntrypoint` should initialize the annotation system:

```java
package com.example.myplugin;

import be.theking90000.mclib2.platform.PlatformEntrypoint;
import be.theking90000.mclib2.runtime.AnnotationBootstrap;
import be.theking90000.mclib2.runtime.AnnotationDiscovery;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin {
    
    @PlatformEntrypoint
    public MyPlugin() {
        // Standalone mode
        initializeAnnotationSystem();
    }
    
    @PlatformEntrypoint
    public MyPlugin(JavaPlugin plugin) {
        // Bukkit mode
        plugin.getLogger().info("Initializing " + plugin.getName() + " with MCLIB2 annotations");
        initializeAnnotationSystem();
    }
    
    private void initializeAnnotationSystem() {
        // Create annotation bootstrap with default factory
        AnnotationBootstrap bootstrap = new AnnotationBootstrap();
        
        // Discover annotations from compile-time generated metadata
        AnnotationDiscovery discovery = new AnnotationDiscovery();
        
        // Bootstrap the annotation system
        bootstrap.bootstrap(discovery.discover());
        
        System.out.println("MCLIB2 annotation system initialized!");
    }
}
```

## Creating Custom Annotations

### 1. Define Your Annotation

Create annotations marked with `@RegisteredAnnotation`:

```java
package com.example.myplugin.annotations;

import be.theking90000.mclib2.annotations.RegisteredAnnotation;
import java.lang.annotation.*;

@RegisteredAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {
    String value() default "";
}

@RegisteredAnnotation
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.TYPE)
public @interface Repository {
    String value() default "";
}

@RegisteredAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventListener {
}
```

### 2. Create Annotation Handlers

Implement `AnnotationHandler` to process classes with your annotations:

```java
package com.example.myplugin.handlers;

import be.theking90000.mclib2.annotations.InjectStrategy;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import be.theking90000.mclib2.runtime.DefaultAnnotationHandlerFactory;
import com.example.myplugin.annotations.Service;

import java.util.Set;

@InjectStrategy(DefaultAnnotationHandlerFactory.class)
public class ServiceHandler implements AnnotationHandler<Service> {
    
    @Override
    public void handle(Class<?> serviceClass) {
        Service annotation = serviceClass.getAnnotation(Service.class);
        String serviceName = annotation.value().isEmpty() ? 
            serviceClass.getSimpleName() : annotation.value();
        
        try {
            // Create service instance
            Object serviceInstance = serviceClass.newInstance();
            
            // Register service (implement your own service registry)
            ServiceRegistry.register(serviceName, serviceInstance);
            
            System.out.println("Registered service: " + serviceName + 
                " (" + serviceClass.getName() + ")");
                
        } catch (Exception e) {
            System.err.println("Failed to instantiate service: " + serviceClass.getName());
            e.printStackTrace();
        }
    }
}
```

### 3. Use Your Annotations

Annotate your classes:

```java
package com.example.myplugin.services;

import com.example.myplugin.annotations.Service;
import com.example.myplugin.annotations.Repository;

@Service("userService")
public class UserService {
    
    public void createUser(String username) {
        System.out.println("Creating user: " + username);
    }
}

@Repository("userRepo")
public class UserRepository {
    
    public void saveUser(String username) {
        System.out.println("Saving user to database: " + username);
    }
}

@Service
public class NotificationService {
    
    public void sendNotification(String message) {
        System.out.println("Sending notification: " + message);
    }
}
```

## Advanced Features

### Custom Dependency Injection

You can integrate with external DI frameworks by implementing custom `AnnotationHandlerFactory`:

```java
package com.example.myplugin.di;

import be.theking90000.mclib2.runtime.AnnotationHandlerFactory;
import be.theking90000.mclib2.runtime.AnnotationHandler;

public class GuiceAnnotationHandlerFactory implements AnnotationHandlerFactory {
    private final Injector injector;
    
    public GuiceAnnotationHandlerFactory(Injector injector) {
        this.injector = injector;
    }
    
    @Override
    public <T extends AnnotationHandler<?>> T create(Class<T> handlerClass) {
        return injector.getInstance(handlerClass);
    }
}

// Usage in your entrypoint:
@PlatformEntrypoint
public MyPlugin() {
    Injector injector = Guice.createInjector(new MyAppModule());
    AnnotationBootstrap bootstrap = new AnnotationBootstrap(
        new GuiceAnnotationHandlerFactory(injector)
    );
    bootstrap.bootstrap(new AnnotationDiscovery().discover());
}
```

### Bukkit Event Listeners with Dependency Injection

Here's a complete example showing how to create Bukkit event listeners with proper JavaPlugin injection:

```java
package com.example.myplugin.handlers;

import be.theking90000.mclib2.annotations.InjectStrategy;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.example.myplugin.annotations.EventListener;
import com.example.myplugin.factory.BukkitAnnotationHandlerFactory;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

// This handler can only be instantiated by BukkitAnnotationHandlerFactory
@InjectStrategy(BukkitAnnotationHandlerFactory.class)
public class BukkitEventListenerHandler implements AnnotationHandler<Listener> {
    private final JavaPlugin plugin;
    
    // Constructor with JavaPlugin dependency injection
    public BukkitEventListenerHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void handle(Class<? extends Listener> listenerClass) throws Exception {
        EventListener annotation = listenerClass.getAnnotation(EventListener.class);
        if (annotation == null) {
            return; // Skip classes without @EventListener
        }
        
        try {
            // Create listener instance
            Listener listener = listenerClass.newInstance();
            
            // Register with Bukkit using the injected plugin instance
            Bukkit.getPluginManager().registerEvents(listener, plugin);
            
            plugin.getLogger().info("Registered event listener: " + listenerClass.getSimpleName());
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register listener: " + listenerClass.getName());
            throw e; // Re-throw to let the framework handle it
        }
    }
    
    @Override
    public void destroy() throws Exception {
        // Cleanup when plugin is disabled
        plugin.getLogger().info("Event listener handler destroyed");
    }
}
```

**Usage - Annotate your Bukkit listeners:**

```java
package com.example.myplugin.listeners;

import com.example.myplugin.annotations.EventListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@EventListener
public class PlayerJoinListener implements Listener {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Welcome! This plugin uses MCLIB2 annotations.");
        System.out.println("Player joined: " + event.getPlayer().getName());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        System.out.println("Player left: " + event.getPlayer().getName());
    }
}

@EventListener
public class ServerEventListener implements Listener {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Log to server console
        event.getPlayer().getServer().getLogger().info(
            "Player " + event.getPlayer().getName() + " joined the server"
        );
    }
}
```

### Factory Pattern Benefits

This pattern provides several advantages:

1. **Platform Isolation**: Handlers requiring Bukkit dependencies won't be instantiated in standalone mode
2. **Dependency Injection**: Handlers receive the dependencies they need automatically
3. **Flexibility**: Different factories can provide different implementations of the same dependencies
4. **Error Handling**: Incompatible handlers are skipped gracefully

### Complete Factory Pattern Example

Here's how all the pieces work together:

```java
// 1. Custom annotation
@RegisteredAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventListener {
}

// 2. Bukkit-specific factory
@InjectStrategy(BukkitAnnotationHandlerFactory.class)
public class BukkitEventListenerHandler implements AnnotationHandler<Listener> {
    private final JavaPlugin plugin;
    
    public BukkitEventListenerHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void handle(Class<? extends Listener> listenerClass) throws Exception {
        // Implementation shown above
    }
}

// 3. Bukkit entrypoint using the factory
@PlatformEntrypoint
public BukkitPlugin(JavaPlugin plugin) {
    // This factory can create handlers that need JavaPlugin
    AnnotationBootstrap bootstrap = new AnnotationBootstrap(
        new BukkitAnnotationHandlerFactory(plugin)
    );
    bootstrap.bootstrap(new AnnotationDiscovery().discover());
}

// 4. Standalone entrypoint using default factory
@PlatformEntrypoint
public StandalonePlugin() {
    // This factory will skip handlers marked with BukkitAnnotationHandlerFactory
    AnnotationBootstrap bootstrap = new AnnotationBootstrap();
    bootstrap.bootstrap(new AnnotationDiscovery().discover());
}
```

## Project Structure with Annotations

```
my-plugin/
├── build.gradle
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/myplugin/
│       │       ├── BukkitPlugin.java           # Bukkit mode entrypoint
│       │       ├── StandalonePlugin.java       # Standalone mode entrypoint
│       │       ├── annotations/
│       │       │   ├── Service.java            # Custom annotations
│       │       │   ├── Repository.java
│       │       │   └── EventListener.java
│       │       ├── factory/
│       │       │   └── BukkitAnnotationHandlerFactory.java  # Custom factory
│       │       ├── handlers/
│       │       │   ├── ServiceHandler.java     # Annotation handlers
│       │       │   ├── RepositoryHandler.java
│       │       │   └── BukkitEventListenerHandler.java
│       │       ├── services/
│       │       │   ├── UserService.java        # Annotated services
│       │       │   └── NotificationService.java
│       │       └── listeners/
│       │           ├── PlayerJoinListener.java # Annotated listeners
│       │           └── ServerEventListener.java
│       └── resources/
│           ├── entrypoints.txt                 # Lists both entrypoint classes
│           ├── plugin.yml                      # Bukkit configuration
│           └── META-INF/services/              # Generated annotation metadata
└── build/
    └── libs/
        ├── my-plugin-bukkit-1.0.0.jar
        └── my-plugin-standalone-1.0.0.jar
```

### Sharing Code Between Platforms

To avoid code duplication between your Bukkit and standalone entrypoints, extract common annotation initialization logic:

```java
// AnnotationInitializer.java - Shared initialization logic
public class AnnotationInitializer {
    
    public static void initializeForBukkit(JavaPlugin plugin) {
        AnnotationBootstrap bootstrap = new AnnotationBootstrap(
            new BukkitAnnotationHandlerFactory(plugin)
        );
        AnnotationDiscovery discovery = new AnnotationDiscovery();
        bootstrap.bootstrap(discovery.discover());
        plugin.getLogger().info("MCLIB2 annotation system initialized for Bukkit!");
    }
    
    public static void initializeForStandalone() {
        AnnotationBootstrap bootstrap = new AnnotationBootstrap();
        AnnotationDiscovery discovery = new AnnotationDiscovery();
        bootstrap.bootstrap(discovery.discover());
        System.out.println("MCLIB2 annotation system initialized for standalone!");
    }
}

// BukkitPlugin.java - Uses shared logic
public class BukkitPlugin {
    @PlatformEntrypoint
    public BukkitPlugin(JavaPlugin plugin) {
        AnnotationInitializer.initializeForBukkit(plugin);
    }
}

// StandalonePlugin.java - Uses shared logic
public class StandalonePlugin {
    @PlatformEntrypoint
    public StandalonePlugin() {
        AnnotationInitializer.initializeForStandalone();
    }
}
```

## Compile-Time Benefits

The annotation framework generates metadata at compile time, providing:

- **Zero-reflection discovery**: No classpath scanning at runtime
- **Faster startup**: Annotations are discovered from pre-generated files
- **Compile-time validation**: Errors are caught during build, not runtime
- **IDE support**: Full autocomplete and refactoring support

## Bootstrap Options

### Default Factory (Simple Constructor Injection)

```java
AnnotationBootstrap bootstrap = new AnnotationBootstrap();
bootstrap.bootstrap(new AnnotationDiscovery().discover());
```

### Custom Factory (Advanced DI)

```java
BukkitAnnotationHandlerFactory factory = new BukkitAnnotationHandlerFactory(plugin);
AnnotationBootstrap bootstrap = new AnnotationBootstrap(factory);
bootstrap.bootstrap(new AnnotationDiscovery().discover());
```

### Manual Handler Registration

For advanced scenarios, you can programmatically control handler registration:

```java
AnnotationBootstrap bootstrap = new AnnotationBootstrap();
AnnotationDiscovery discovery = new AnnotationDiscovery();

// Get discovered data
var discoveryResult = discovery.discover();

// You could filter or modify the discovery results here if needed
// (though this is rarely necessary)

bootstrap.bootstrap(discoveryResult);
```

## Understanding the AnnotationHandlerFactory Pattern

The `AnnotationHandlerFactory` is the core abstraction that enables pluggable dependency injection in MCLIB2. It controls how annotation handlers are instantiated, allowing different DI frameworks to coexist.

### The Factory Interface

```java
public interface AnnotationHandlerFactory {
    /**
     * Creates an instance of the specified annotation handler class.
     * This is where different DI strategies are implemented.
     */
    <T extends AnnotationHandler<?>> T create(Class<T> handlerClass);
}
```

### Factory Hierarchy

MCLIB2 provides several factory implementations:

1. **DefaultAnnotationHandlerFactory** - Simple no-arg constructor instantiation
2. **Custom Platform Factories** - Inject platform-specific dependencies
3. **External DI Factories** - Integration with Guice, Spring, etc.

### Default Factory Implementation

```java
public class DefaultAnnotationHandlerFactory implements AnnotationHandlerFactory {
    @Override
    public <T extends AnnotationHandler<?>> T create(Class<T> handlerClass) {
        try {
            return handlerClass.newInstance(); // Simple no-arg constructor
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate handler: " + handlerClass.getName(), e);
        }
    }
}
```

### Custom Bukkit Factory Implementation

Here's how to create a platform-specific factory that can inject Bukkit dependencies:

```java
package com.example.myplugin.factory;

import be.theking90000.mclib2.runtime.AnnotationHandlerFactory;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import org.bukkit.plugin.java.JavaPlugin;
import java.lang.reflect.Constructor;

public class BukkitAnnotationHandlerFactory implements AnnotationHandlerFactory {
    private final JavaPlugin plugin;
    
    public BukkitAnnotationHandlerFactory(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public <T extends AnnotationHandler<?>> T create(Class<T> handlerClass) {
        try {
            // Try constructor with JavaPlugin parameter first
            try {
                Constructor<T> pluginConstructor = handlerClass.getConstructor(JavaPlugin.class);
                return pluginConstructor.newInstance(plugin);
            } catch (NoSuchMethodException e) {
                // Fall back to no-arg constructor
                return handlerClass.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate handler: " + handlerClass.getName(), e);
        }
    }
}
```

### Factory Selection with @InjectStrategy

Handlers specify which factory can instantiate them using `@InjectStrategy`:

```java
// This handler can only be created by BukkitAnnotationHandlerFactory
@InjectStrategy(BukkitAnnotationHandlerFactory.class) 
public class BukkitEventListenerHandler implements AnnotationHandler<Listener> {
    private final JavaPlugin plugin;
    
    public BukkitEventListenerHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    // Handler implementation...
}

// This handler works with any factory (uses default)
@InjectStrategy(DefaultAnnotationHandlerFactory.class)
public class ServiceHandler implements AnnotationHandler<Service> {
    public ServiceHandler() {
        // No dependencies required
    }
    
    // Handler implementation...
}
```

### Factory Matching Process

When the annotation system bootstraps:

1. **Discovery**: Finds all handlers and their `@InjectStrategy` annotations
2. **Factory Matching**: Only creates handlers whose `@InjectStrategy` matches the current factory
3. **Instantiation**: Uses the factory to create handler instances
4. **Processing**: Calls `handle()` for each discovered annotated class

This ensures that:
- Bukkit-specific handlers are only created when running in Bukkit mode
- Standalone handlers work everywhere
- Dependencies are properly injected based on the current platform

This annotation-driven approach allows you to build complex, modular applications with automatic service discovery and dependency injection while maintaining excellent performance through compile-time processing.
