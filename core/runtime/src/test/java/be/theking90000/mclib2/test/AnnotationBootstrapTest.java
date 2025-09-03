package be.theking90000.mclib2.test;

import be.theking90000.mclib2.runtime.AnnotationBootstrap;
import be.theking90000.mclib2.runtime.AnnotationDiscovery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnnotationBootstrapTest {

    AnnotationDiscovery discovery = new AnnotationDiscovery();

    @Test
    void testBootstrapInvokesHandler() {
        // Clear previous state
        TestServiceHandler.handled.clear();
        TestServiceHandlerFactory.handled.clear();

        // Run bootstrap with default factory
        AnnotationBootstrap bootstrap = new AnnotationBootstrap();

        AnnotationDiscovery.AnnotationResult result = discovery.discover();

        bootstrap.bootstrap(result);

        assertEquals(0, TestServiceHandlerFactory.handled.size());
        // Assert that our handler was called with the annotated class
        assertEquals(1, TestServiceHandler.handled.size());
        assertEquals("be.theking90000.mclib2.test.TestService",
                TestServiceHandler.handled.get(0).getName());

        bootstrap.shutdown();

        assertEquals(0, TestServiceHandler.handled.size());

    }

    @Test
    void testBootstrapInvokesHandlerWithFactory() {
        // Clear previous state
        TestServiceHandler.handled.clear();
        TestServiceHandlerFactory.handled.clear();

        AnnotationBootstrap bootstrap = new AnnotationBootstrap(new TestFactory());

        AnnotationDiscovery.AnnotationResult result = discovery.discover();

        bootstrap.bootstrap(result);

        // Assert that our handler was called with the annotated class
        assertEquals(0, TestFactory.destroyed);
        assertEquals(1, TestServiceHandlerFactory.handled.size());
        assertEquals(1, TestFactory.created.size());
        assertEquals("be.theking90000.mclib2.test.TestServiceHandlerFactory",
                TestFactory.created.get(0).getName());

        assertEquals(0, TestServiceHandler.handled.size());
        assertEquals("be.theking90000.mclib2.test.TestService",
                TestServiceHandlerFactory.handled.get(0).getName());

        bootstrap.shutdown();

        assertEquals(0, TestFactory.created.size());
        assertEquals(1, TestFactory.destroyed);
        assertEquals(1, TestServiceHandlerFactory.handled.size());
    }
}
