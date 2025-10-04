package be.theking90000.mclib2.test;

import be.theking90000.mclib2.integration.guice.GuiceModule;
import com.google.inject.AbstractModule;

import java.util.logging.Logger;

@GuiceModule
public class TestModule extends AbstractModule {

    private static final Logger LOGGER = Logger.getLogger("TestModule");

    public TestModule() {
        LOGGER.info("TestModule::new()");
    }

    @Override
    protected void configure() {
        LOGGER.info("TestModule::configure()");
    }
}
