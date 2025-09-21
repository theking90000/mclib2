package be.theking90000.mclib2.integration;

import be.theking90000.mclib2.platform.PlatformEntrypoint;
import be.theking90000.mclib2.runtime.AnnotationBootstrap;
import be.theking90000.mclib2.runtime.AnnotationDiscovery;

public class CommonEntrypoint {

    @PlatformEntrypoint
    public CommonEntrypoint() {
        System.out.println("Hello From CommonEntrypoint !");

        AnnotationDiscovery ad = new AnnotationDiscovery();
        AnnotationDiscovery.AnnotationResult ar = ad.discover();

        AnnotationBootstrap bs = new AnnotationBootstrap();

        bs.bootstrap(ar);
    }

}
