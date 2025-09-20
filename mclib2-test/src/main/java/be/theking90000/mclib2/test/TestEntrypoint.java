package be.theking90000.mclib2.test;

import be.theking90000.mclib2.platform.PlatformEntrypoint;

import java.util.logging.Logger;

public class TestEntrypoint {
    public static String test = "Test";

    Logger logger = Logger.getLogger("TestEntrypoint");

    @PlatformEntrypoint
    public TestEntrypoint() {
        logger.info( "TestEntrypoint initialized");
    }
}
