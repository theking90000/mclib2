package be.theking90000.mclib2.integration.guice;

import be.theking90000.mclib2.annotations.AnnotationLoader;
import be.theking90000.mclib2.annotations.InjectStrategy;
import be.theking90000.mclib2.runtime.AnnotationHandler;
import com.google.inject.Module;

import java.util.Set;

@AnnotationLoader(GuiceModule.class)
@InjectStrategy(GuiceModuleAnnotationHandlerFactory.class)
public class GuiceModuleAnnotationHandler implements AnnotationHandler<Module> {

    private final Set<Module> modules;

    public GuiceModuleAnnotationHandler(Set<Module> modules) {
        this.modules = modules;
    }

    @Override
    public void handle(Class<? extends Module> clazz) throws Exception {
        try {
            modules.add(clazz.getDeclaredConstructor().newInstance());
        } catch (NoSuchMethodException e) {
            //
        }
    }

}
