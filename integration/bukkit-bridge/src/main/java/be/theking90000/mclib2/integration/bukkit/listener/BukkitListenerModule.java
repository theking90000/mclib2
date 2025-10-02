package be.theking90000.mclib2.integration.bukkit.listener;

import be.theking90000.mclib2.inject.AbstractCloseableModule;
import be.theking90000.mclib2.integration.GuiceModule;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

@GuiceModule
public class BukkitListenerModule extends AbstractCloseableModule {

    @Override
    protected void configure() {
        BukkitListenerProvisionListener listener = new BukkitListenerProvisionListener();

        requestInjection(listener);

        bindListener(Matchers.any(), listener);
        bindListener(listener);
    }
}
