package be.theking90000.mclib2.test;

import be.theking90000.mclib2.annotations.AnnotationLoader;
import be.theking90000.mclib2.runtime.AnnotationHandler;

import java.util.ArrayList;
import java.util.List;

@AnnotationLoader(Service.class)
public class TestServiceHandler implements AnnotationHandler<Service> {
    public static final List<Class<?>> handled = new ArrayList<>();

    @Override
    public void handle(Class<? extends Service> clazz) throws Exception {
        handled.add(clazz);
    }
}
