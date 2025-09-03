package be.theking90000.mclib2.runtime;

import be.theking90000.mclib2.annotations.AnnotationLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;

/**
 * Responsible for discovering annotation metadata generated at compile-time.
 *
 * <p>This class reads:
 * <ul>
 *   <li>{@code META-INF/services/be.theking90000.mclib2.runtime.AnnotationHandler}
 *       to find all available handler classes.</li>
 *   <li>{@code META-INF/annotations/<AnnotationName>}
 *       to find all classes annotated with a given annotation.</li>
 * </ul>
 *
 * <p>The result is returned as an {@link AnnotationResult}, which can then
 * be consumed by the runtime to instantiate handlers and process classes.</p>
 */
public class AnnotationDiscovery {

    private final ClassLoader classLoader;

    public AnnotationDiscovery(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public AnnotationDiscovery() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Performs discovery of handlers and annotated classes.
     *
     * @return an {@link AnnotationResult} containing all discovered metadata
     */
    public AnnotationResult discover() {
        AnnotationResult result = new AnnotationResult();
        discoverHandlers(result);
        discoverAnnotatedClasses(result);
        return result;
    }

    private void discoverHandlers(AnnotationResult result) {
        try {
            Enumeration<URL> resources = classLoader.getResources(
                    "META-INF/services/" + AnnotationHandler.class.getCanonicalName()
            );

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    reader.lines()
                            .map(String::trim)
                            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                            .forEach(className -> {
                                try {
                                    @SuppressWarnings("unchecked")
                                    Class<? extends AnnotationHandler<?>> handlerClass =
                                            (Class<? extends AnnotationHandler<?>>) Class.forName(className, true, classLoader);

                                    // Find the annotation this handler is for
                                    AnnotationLoader annotationLoader = handlerClass.getAnnotation(AnnotationLoader.class);

                                    result.addHandler(annotationLoader.value(), handlerClass);
                                } catch (Exception e) {
                                    throw new RuntimeException("Failed to load handler: " + className, e);
                                }
                            });
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to discover handlers", e);
        }
    }

    private void discoverAnnotatedClasses(AnnotationResult result) {
        try {
            Enumeration<URL> resources = classLoader.getResources("META-INF/annotations-mappings.txt");
            while (resources.hasMoreElements()) {
                URL dirUrl = resources.nextElement();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(dirUrl.openStream()))) {
                    reader.lines()
                            .map(String::trim)
                            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                            .map(line -> line.split(" -> "))
                            .forEach(arr -> {
                                try {
                                    String annotationName = arr[0];
                                    String className = arr[1];

                                    Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) Class.forName(annotationName, true, classLoader);

                                    Class<?> clazz = Class.forName(className, true, classLoader);

                                    result.addAnnotatedClass(annotationClass, clazz);
                                } catch (Exception e) {
                                    throw new RuntimeException("Failed to load handler: " + arr[1], e);
                                }
                            });
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to discover annotated classes", e);
        }
    }


    /**
     * Holds the result of an annotation discovery pass.
     *
     * <p>It maps each discovered annotation type to:
     * <ul>
     *   <li>The {@link AnnotationHandler} classes that can handle it.</li>
     *   <li>The application classes annotated with it.</li>
     * </ul>
     *
     * <p>This is a pure data structure: it does not instantiate handlers
     * or execute them. That responsibility belongs to the runtime.</p>
     */
    public static class AnnotationResult {

        private final Map<Class<? extends Annotation>, Set<Class<? extends AnnotationHandler<?>>>> handlersByAnnotation = new HashMap<>();
        private final Map<Class<? extends Annotation>, Set<Class<?>>> classesByAnnotation = new HashMap<>();

        /**
         * Registers a handler class for a given annotation.
         *
         * @param annotation   the annotation class
         * @param handlerClass the handler class
         */
        public void addHandler(Class<? extends Annotation> annotation, Class<? extends AnnotationHandler<?>> handlerClass) {
            handlersByAnnotation
                    .computeIfAbsent(annotation, k -> new HashSet<>())
                    .add(handlerClass);
        }

        /**
         * Registers an annotated class for a given annotation.
         *
         * @param annotation the annotation class
         * @param clazz      the annotated class
         */
        public void addAnnotatedClass(Class<? extends Annotation> annotation, Class<?> clazz) {
            classesByAnnotation
                    .computeIfAbsent(annotation, k -> new HashSet<>())
                    .add(clazz);
        }

        /**
         * @return all annotation names discovered
         */
        public Set<Class<? extends Annotation>> getDiscoveredAnnotations() {
            Set<Class<? extends Annotation>> all = new HashSet<>();
            all.addAll(handlersByAnnotation.keySet());
            all.addAll(classesByAnnotation.keySet());
            return all;
        }

        /**
         * @param annotation annotation class
         * @return handler classes for this annotation
         */
        public Set<Class<? extends AnnotationHandler<?>>> getHandlers(Class<? extends Annotation> annotation) {
            return handlersByAnnotation.getOrDefault(annotation, Collections.emptySet());
        }

        /**
         * @param annotation annotation class
         * @return annotated classes for this annotation
         */
        public Set<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotation) {
            return classesByAnnotation.getOrDefault(annotation, Collections.emptySet());
        }

    }

}
