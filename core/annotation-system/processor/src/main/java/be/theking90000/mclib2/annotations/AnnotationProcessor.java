package be.theking90000.mclib2.annotations;

import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Annotation processor for the mclib2 modular system.
 *
 * <p>This processor has 3 responsibilities:</p>
 * <ol>
 *   <li>Detect annotations marked with {@link RegisteredAnnotation}.</li>
 *   <li>Detect classes annotated with {@link AnnotationLoader} and
 *       generate a ServiceLoader file for {@link AnnotationHandler}.</li>
 *   <li>Detect classes annotated with any {@link RegisteredAnnotation}
 *       and generate mapping files under META-INF/annotations/.</li>
 * </ol>
 *
 * <p>The result is a set of metadata files that the runtime can read
 * without scanning the classpath.</p>
 */
@SupportedAnnotationTypes("*") // we scan everything
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {
    private Filer filer;
    private Elements elementUtils;
    private Types typeUtil;

    private Map<String, Set<String>> annotationMappings = new HashMap<>();
    private Set<String> loaders = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtil = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotationMappings.clear();
        loaders.clear();

        processRegisteredAnnotations(roundEnv);
        processAnnotationLoaders(roundEnv);

        try {
            writeLoaders();
            writeAnnotationMappings();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private void processRegisteredAnnotations(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
                TypeElement annotation = (TypeElement) mirror.getAnnotationType().asElement();

                if (annotation.getAnnotation(RegisteredAnnotation.class) != null) {
                    String annotationName = annotation.getQualifiedName().toString();

                    annotationMappings.computeIfAbsent(annotationName, k -> new HashSet<>())
                            .add(((TypeElement) element).getQualifiedName().toString());
                }
            }
        }
    }

    private void processAnnotationLoaders(RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(AnnotationLoader.class)) {
            if (e.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) e;
                TypeMirror handlerType = typeUtil.erasure(
                        elementUtils.getTypeElement(AnnotationHandler.class.getCanonicalName()).asType()
                );
                // Erase the class type too
                TypeMirror classType = typeUtil.erasure(typeElement.asType());

                if (!typeUtil.isAssignable(classType, handlerType)) {
                    throw new IllegalArgumentException(typeElement.getQualifiedName() + " must implement AnnotationHandler");
                }

                if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
                    throw new IllegalArgumentException(
                            typeElement.getQualifiedName() + " must not be abstract"
                    );
                }

                String className = typeElement.getQualifiedName().toString();
                loaders.add(className);
            }
        }
    }

    private void writeLoaders() throws IOException {
        if (!loaders.isEmpty()) {
            String resourceFile = "META-INF/services/" + AnnotationHandler.class.getCanonicalName();
            FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceFile);

            try (OutputStream os = fileObject.openOutputStream();
                 Writer writer = new OutputStreamWriter(os)) {
                for (String loader : loaders) {
                    writer.write(loader);
                    writer.write("\n");
                }
            }
        }
    }

    private void writeAnnotationMappings() throws IOException {
        if(annotationMappings.isEmpty()) return;

        for (Map.Entry<String, Set<String>> entry : annotationMappings.entrySet()) {
            String annotation = entry.getKey();
            Set<String> classes = entry.getValue();

            // e.g. META-INF/annotations/com.example.Service
            FileObject file = filer.createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    "META-INF/annotations/" + annotation
            );

            try (Writer writer = file.openWriter()) {
                for (String clazz : classes) {
                    writer.write(clazz + "\n");
                }
            }
        }

        FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/annotations-mappings.txt");
        try (Writer writer = file.openWriter()) {
            for (Map.Entry<String, Set<String>> entry : annotationMappings.entrySet()) {
                String annotation = entry.getKey();
                Set<String> classes = entry.getValue();

                for (String clazz : classes) {
                    writer.write(annotation + " -> " + clazz + "\n");
                }

            }
        }
    }

}
