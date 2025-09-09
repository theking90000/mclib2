package be.theking90000.mclib2.test;


import be.theking90000.mclib2.platform.PlatformEntrypoint;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Logger;

public class TestEntrypoint {
    public static String test = "Test";

    Logger logger = Logger.getLogger("TestEntrypoint");

    @PlatformEntrypoint
    public TestEntrypoint() {
        System.out.println("TestEntrypoint initialized");
        logger.info( "TestEntrypoint initialized");
    }
}
