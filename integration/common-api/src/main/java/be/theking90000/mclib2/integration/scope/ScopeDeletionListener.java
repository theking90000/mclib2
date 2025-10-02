package be.theking90000.mclib2.integration.scope;

import com.google.inject.Scope;

public interface ScopeDeletionListener {

    void onScopeDeletion(Scope scope);

}
