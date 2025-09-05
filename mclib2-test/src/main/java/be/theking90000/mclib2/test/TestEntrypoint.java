package be.theking90000.mclib2.test;


import be.theking90000.mclib2.platform.PlatformEntrypoint;

public class TestEntrypoint {
    public static String test = "Test";

    @PlatformEntrypoint
    public TestEntrypoint() {
        System.out.println("TestEntrypoint initialized");
    }
}
