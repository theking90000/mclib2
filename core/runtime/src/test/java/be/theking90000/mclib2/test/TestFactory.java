package be.theking90000.mclib2.test;

import be.theking90000.mclib2.runtime.AbstractAnnotationHandlerFactory;
import be.theking90000.mclib2.runtime.AnnotationHandler;

import java.util.ArrayList;
import java.util.List;

public class TestFactory extends AbstractAnnotationHandlerFactory {

    public static final List<Class<?>> created = new ArrayList<>();
    public static int destroyed = 0;

    @Override
    public <T extends AnnotationHandler<V>, V> T create(Class<T> loaderClass) throws Exception {
        created.add(loaderClass);

        return loaderClass.getDeclaredConstructor().newInstance();
    }

    @Override
    public <T extends AnnotationHandler<V>, V> void destroy(T handler) throws Exception {
        created.remove(handler.getClass());
        destroyed++;
    }
}
