package be.theking90000.mclib2.inject;

import be.theking90000.mclib2.inject.listener.CloseableDisposeListener;
import be.theking90000.mclib2.inject.listener.CloseableProvisionListener;
import com.google.inject.matcher.Matchers;

public class CloseableModule extends AbstractCloseableModule {

    private final CloseableRegistry registry;

    protected CloseableModule(CloseableRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void configure() {
        bindListener(Matchers.any(), new CloseableProvisionListener(registry));

        bind(CloseableRegistry.class).toInstance(registry);
        bind(CloseableInjector.class).toProvider(registry::getInjector);

        bindListener(new CloseableDisposeListener());
    }
}
