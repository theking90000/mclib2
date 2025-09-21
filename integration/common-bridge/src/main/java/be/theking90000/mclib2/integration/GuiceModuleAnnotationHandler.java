package be.theking90000.mclib2.integration;

import be.theking90000.mclib2.annotations.AnnotationLoader;
import be.theking90000.mclib2.runtime.AnnotationHandler;

@AnnotationLoader(GuiceModule.class)
public class GuiceModuleAnnotationHandler implements AnnotationHandler<GuiceModule> {

    public GuiceModuleAnnotationHandler() {
        System.out.println("Loaded GuiceModuleAnnotationHandler");
    }

    @Override
    public void handle(Class<? extends GuiceModule> clazz) throws Exception {
        System.out.println("Loaded GuiceModule: " + clazz.getCanonicalName());
    }
}
