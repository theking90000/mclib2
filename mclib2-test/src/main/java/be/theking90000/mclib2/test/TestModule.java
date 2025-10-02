package be.theking90000.mclib2.test;

import be.theking90000.mclib2.integration.guice.GuiceModule;
import com.google.inject.AbstractModule;

@GuiceModule
public class TestModule extends AbstractModule {

    public TestModule() {
        System.out.println("TestModule::new()");
    }

    @Override
    protected void configure() {
        System.out.println("TestModule::configure()");
    }
}
