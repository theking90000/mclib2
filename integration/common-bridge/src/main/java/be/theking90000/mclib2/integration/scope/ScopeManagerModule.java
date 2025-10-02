package be.theking90000.mclib2.integration.scope;

import be.theking90000.mclib2.integration.guice.GuiceModule;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

@GuiceModule
public class ScopeManagerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ScopeManagerImpl.class).in(Singleton.class);
        bind(ScopeManager.class).to(ScopeManagerImpl.class).in(Singleton.class);
    }
}
