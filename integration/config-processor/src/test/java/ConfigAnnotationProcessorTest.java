import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class ConfigAnnotationProcessorTest {

    @Test
    void testProcessor() {
        // Define a class annotated with @Service
        JavaFileObject annotatedClass = JavaFileObjects.forSourceString(
                "be.theking90000.test.CfgTest",
                "package be.theking90000.test;\n" +
                        "import be.theking90000.mclib2.config.Config;\n" +
                        "@Config\n" +
                        "public class CfgTest {}\n"
        );


        // Run compilation with your processor
        Compilation compilation = Compiler.javac()
                .compile(annotatedClass);


        // Assert compilation succeeded
        assertThat(compilation).succeeded();

        System.out.println(compilation.generatedFiles());

        // Assert that the processor generated the expected metadata file
       /* assertThat(compilation)
                .generatedFile(
                        javax.tools.StandardLocation.CLASS_OUTPUT,
                        "META-INF/annotations/be.theking90000.test.Service"
                )
                .contentsAsUtf8String()
                .contains("be.theking90000.test.PlayerMoneyService");*/
    }

}
