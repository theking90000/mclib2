package be.theking90000.mclib2.annotations;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class RegisteredAnnotationsProcessorTest {

    private JavaFileObject serviceAnnotation = JavaFileObjects.forSourceString(
            "be.theking90000.test.Service",
            "package be.theking90000.test;\n" +
                    "import be.theking90000.mclib2.annotations.RegisteredAnnotation;\n" +
                    "import java.lang.annotation.*;\n" +
                    "@RegisteredAnnotation\n" +
                    "@Retention(RetentionPolicy.RUNTIME)\n" +
                    "@Target(ElementType.TYPE)\n" +
                    "public @interface Service {}\n"
    );

    @Test
    void testServiceAnnotationGeneratesMetadata() {
        // Define a class annotated with @Service
        JavaFileObject annotatedClass = JavaFileObjects.forSourceString(
                "be.theking90000.test.PlayerMoneyService",
                "package be.theking90000.test;\n" +
                        "@Service\n" +
                        "public class PlayerMoneyService {}\n"
        );



        // Run compilation with your processor
        Compilation compilation = Compiler.javac()
                .withProcessors(new AnnotationProcessor())
                .compile(serviceAnnotation, annotatedClass);

        // Assert compilation succeeded
        assertThat(compilation).succeeded();

        System.out.println(compilation.generatedFiles());

        // Assert that the processor generated the expected metadata file
        assertThat(compilation)
                .generatedFile(
                        javax.tools.StandardLocation.CLASS_OUTPUT,
                        "META-INF/annotations/be.theking90000.test.Service"
                )
                .contentsAsUtf8String()
                .contains("be.theking90000.test.PlayerMoneyService");

        assertThat(compilation)
                .generatedFile(
                        javax.tools.StandardLocation.CLASS_OUTPUT,
                        "META-INF/annotations-mappings.txt"
                )
                .contentsAsUtf8String()
                .contains("be.theking90000.test.Service -> be.theking90000.test.PlayerMoneyService");

    }

    @Test
    void testAnnotationLoaderMustImplementInterface() {
        JavaFileObject invalidLoader = JavaFileObjects.forSourceString(
                "be.theking90000.test.ServiceLoader",
                "package be.theking90000.test;\n" +
                        "import be.theking90000.mclib2.annotations.AnnotationLoader;\n" +
                        "@AnnotationLoader(Service.class)\n" +
                        "public class ServiceLoader {}\n"
        );

        try {
            Compilation compilation = Compiler.javac()
                    .withProcessors(new AnnotationProcessor())
                    .compile(invalidLoader, serviceAnnotation);

            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("must implement AnnotationHandler");
        }
    }

    @Test
    void testAnnotationLoaderMustNotBeAbstract() {
        JavaFileObject invalidLoader = JavaFileObjects.forSourceString(
                "be.theking90000.test.ServiceLoader",
                "package be.theking90000.test;\n" +
                        "import be.theking90000.mclib2.annotations.AnnotationLoader;\n" +
                        "import be.theking90000.mclib2.annotations.AnnotationHandler;\n" +
                        "@AnnotationLoader(Service.class)\n" +
                        "public abstract class ServiceLoader implements AnnotationHandler<Service> {}\n"
        );

        try {
            Compilation compilation = Compiler.javac()
                    .withProcessors(new AnnotationProcessor())
                    .compile(invalidLoader, serviceAnnotation);

            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("must not be abstract");
        }
    }

    @Test
    void testAnnotationLoaderGeneratesServiceFile() {
        JavaFileObject validLoader = JavaFileObjects.forSourceString(
                "be.theking90000.test.ServiceLoader",
                "package be.theking90000.test;\n" +
                        "import be.theking90000.mclib2.annotations.AnnotationLoader;\n" +
                        "import be.theking90000.mclib2.annotations.AnnotationHandler;\n" +
                        "@AnnotationLoader(Service.class)\n" +
                        "public class ServiceLoader implements AnnotationHandler<Service> {\n" +
                        "    public void handle(Class<? extends Service> clazz) {}\n" +
                        "}\n"
        );

        Compilation compilation = Compiler.javac()
                .withProcessors(new AnnotationProcessor())
                .compile(validLoader, serviceAnnotation);

        assertThat(compilation).succeeded();

        assertThat(compilation)
                .generatedFile(
                        javax.tools.StandardLocation.CLASS_OUTPUT,
                        "META-INF/services/be.theking90000.mclib2.annotations.AnnotationHandler"
                )
                .contentsAsUtf8String()
                .contains("be.theking90000.test.ServiceLoader");
    }

}
