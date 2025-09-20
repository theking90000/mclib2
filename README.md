# MCLIB2

**A Java Development Toolkit for Minecraft Plugin Development with Standalone Support**

MCLIB2 is a comprehensive development framework designed primarily for Minecraft plugin development while maintaining the flexibility to run in standalone mode. It provides a layered architecture that simplifies plugin development through advanced annotation processing, dependency injection, and multi-platform packaging capabilities.

## 🏗️ Architecture Overview

MCLIB2 follows a three-layer architecture that separates concerns and provides maximum flexibility:

### 1. Platform Layer
The foundation layer that handles compilation, packaging, and deployment across different platforms.

- **Gradle Plugin Integration**: Simplifies build configuration and dependency management
- **Multi-Platform Support**: 
  - Bukkit/Spigot plugins
  - Standalone applications
  - Extensible for future platforms (Velocity, etc.)
- **Dependency Management**: Automatic dependency resolution and version management
- **Smart Packaging**: Generates platform-specific JAR files with proper manifests and descriptors

### 2. Core Layer
The annotation processing engine that enables compile-time service discovery and runtime handler management.

- **Annotation System**: Meta-annotation framework using `@RegisteredAnnotation`
- **Compile-time Processing**: Generates metadata for efficient runtime discovery
- **Handler Factory Pattern**: Pluggable instantiation strategies (default, Guice, Spring, etc.)
- **Service Discovery**: Zero-reflection annotation scanning through pre-generated metadata

### 3. Framework Layer *(Coming Soon)*
A complete Dependency Injection framework with batteries included for Minecraft plugin development.

- **Full DI Container**: Complete dependency injection with lifecycle management
- **Plugin Lifecycle**: Automatic handling of plugin enable/disable phases
- **Event System**: Streamlined event handling and registration
- **Configuration Management**: Type-safe configuration binding

## 🚀 Getting Started

MCLIB2 can be used in two main modes depending on your needs:

### 📋 Platform Mode
Use MCLIB2's build system and platform adapters without the annotation framework. Perfect for simple plugins or when using your own DI system.

**→ See [Platform Mode Documentation](docs/platform.md)**

### 🔧 Annotation Framework Mode  
Use the full MCLIB2 experience with compile-time annotation processing, automatic service discovery, and dependency injection.

**→ See [Annotation Framework Documentation](docs/annotation.md)**

## 📦 Project Structure

```
mclib2/
├── core/                          # Core annotation processing system
│   ├── annotation-system/
│   │   ├── api/                   # Annotation definitions and interfaces
│   │   └── processor/             # Compile-time annotation processor
│   └── runtime/                   # Runtime annotation discovery and bootstrapping
├── platform/                     # Platform-specific adapters and tooling
│   ├── boot/                      # Platform bootstrap system
│   ├── gradle-plugin/             # Gradle plugin for build integration
│   ├── bukkit-adapter/            # Bukkit/Spigot platform adapter
│   └── standalone-adapter/        # Standalone application adapter
└── mclib2-test/                   # Example project and integration tests
```

## 🔧 Core Components

### Annotation System

The heart of MCLIB2 is its annotation processing system:

- **`@RegisteredAnnotation`**: Meta-annotation that marks other annotations as discoverable
- **`@InjectStrategy`**: Specifies which factory should instantiate annotation handlers
- **`@PlatformEntrypoint`**: Marks constructors as platform entry points

### Runtime Discovery

The runtime system provides efficient annotation discovery without classpath scanning:

- **`AnnotationDiscovery`**: Discovers registered annotations and their handlers from generated metadata
- **`AnnotationBootstrap`**: High-level API for initializing the annotation system
- **`AnnotationHandlerFactory`**: Pluggable factory system for different DI frameworks

### Platform Integration

The platform layer handles the complexity of multi-platform deployment:

- **Gradle Plugin**: Automatically configures dependencies and build tasks
- **Dependency Resolution**: Smart resolution of MCLIB2 components and versions
- **JAR Generation**: Creates platform-specific artifacts (bukkit, standalone, etc.)

## 🎯 Key Features

### ✨ Zero-Reflection Discovery
Unlike traditional frameworks that scan the entire classpath at runtime, MCLIB2 generates discovery metadata at compile-time, resulting in:
- Faster startup times
- Reduced memory usage
- Compile-time validation

### 🔌 Pluggable DI Integration
MCLIB2 doesn't force you into a specific DI framework. It supports:
- Default factory (simple constructor injection)
- Guice integration
- Spring integration
- Custom factory implementations

### 🏗️ Multi-Platform Support
Write once, deploy anywhere:
- Bukkit/Spigot plugins
- Standalone Java applications
- Future platform support (Velocity, Fabric, etc.)

### 📋 Build Simplification
The Gradle plugin handles:
- Dependency version management
- Platform-specific packaging
- Descriptor generation
- Resource processing

## 📚 Examples

### Custom Service Annotation

```java
@RegisteredAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Repository {
}

@Repository
public class UserRepository {
    public List<User> findAll() {
        // Implementation
    }
}

// Handler for @Repository annotations
@InjectStrategy(DefaultAnnotationHandlerFactory.class)
public class RepositoryHandler implements AnnotationHandler<Repository> {
    @Override
    public void handle(Set<Class<?>> annotatedClasses) {
        for (Class<?> clazz : annotatedClasses) {
            // Register repository instances
            registerRepository(clazz);
        }
    }
}
```

### Bukkit Plugin Integration

```java
public class MyBukkitPlugin {
    @PlatformEntrypoint
    public MyBukkitPlugin(Plugin bukkitPlugin) {
        // Initialize with Bukkit plugin instance
        AnnotationBootstrap bootstrap = new AnnotationBootstrap();
        bootstrap.bootstrap(new AnnotationDiscovery().discover());
    }
}
```

## 🛠️ Development Status

**Current Version**: 0.0.1 (Early Development)

### ✅ Completed
- Core annotation processing system
- Gradle plugin infrastructure  
- Bukkit platform adapter
- Standalone platform adapter
- Basic runtime discovery

### 🚧 In Progress
- Complete Framework Layer with DI container
- Advanced configuration management
- Event system integration

### 📋 Planned
- Velocity platform support
- Fabric platform support
- Spring integration module
- Comprehensive documentation and examples

## 🤝 Contributing

MCLIB2 is in active development. Contributions are welcome!

### Building from Source

```bash
git clone https://github.com/yourusername/mclib2.git
cd mclib2
./gradlew build
```

### Testing

The project includes a test module (`mclib2-test`) that demonstrates usage:

```bash
./gradlew :mclib2-test:build
./gradlew :mclib2-test:serverPlugin  # Copies plugin to test server
```

## 📄 License

[Add your license information here]

## 🔗 Links

- [Documentation](docs/) *(Coming Soon)*
- [Examples](examples/) *(Coming Soon)*
- [API Reference](api-docs/) *(Coming Soon)*

---

**MCLIB2** - Simplifying Minecraft plugin development through modern Java practices and tooling.
