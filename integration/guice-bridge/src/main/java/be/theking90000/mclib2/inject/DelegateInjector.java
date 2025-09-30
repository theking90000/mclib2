package be.theking90000.mclib2.inject;

import com.google.inject.*;
import com.google.inject.spi.Element;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.TypeConverterBinding;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DelegateInjector implements Injector {

    private final Injector delegate;

    public DelegateInjector(Injector delegate) {
        this.delegate = delegate;
    }

    @Override
    public void injectMembers(Object instance) {
        delegate.injectMembers(instance);
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
        return delegate.getMembersInjector(typeLiteral);
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
        return delegate.getMembersInjector(type);
    }

    @Override
    public Map<Key<?>, Binding<?>> getBindings() {
        return delegate.getBindings();
    }

    @Override
    public Map<Key<?>, Binding<?>> getAllBindings() {
        return delegate.getAllBindings();
    }

    @Override
    public <T> Binding<T> getBinding(Key<T> key) {
        return delegate.getBinding(key);
    }

    @Override
    public <T> Binding<T> getBinding(Class<T> type) {
        return delegate.getBinding(type);
    }

    @Override
    public <T> Binding<T> getExistingBinding(Key<T> key) {
        return delegate.getExistingBinding(key);
    }

    @Override
    public <T> List<Binding<T>> findBindingsByType(TypeLiteral<T> type) {
        return delegate.findBindingsByType(type);
    }

    @Override
    public <T> Provider<T> getProvider(Key<T> key) {
        return delegate.getProvider(key);
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        return delegate.getProvider(type);
    }

    @Override
    public <T> T getInstance(Key<T> key) {
        return delegate.getInstance(key);
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return delegate.getInstance(type);
    }

    @Override
    public Injector getParent() {
        return delegate.getParent();
    }

    @Override
    public Injector createChildInjector(Iterable<? extends Module> modules) {
        return delegate.createChildInjector(modules);
    }

    @Override
    public Injector createChildInjector(Module... modules) {
        return delegate.createChildInjector(modules);
    }

    @Override
    public Map<Class<? extends Annotation>, Scope> getScopeBindings() {
        return delegate.getScopeBindings();
    }

    @Override
    public Set<TypeConverterBinding> getTypeConverterBindings() {
        return delegate.getTypeConverterBindings();
    }

    @Override
    public List<Element> getElements() {
        return delegate.getElements();
    }

    @Override
    public Map<TypeLiteral<?>, List<InjectionPoint>> getAllMembersInjectorInjectionPoints() {
        return delegate.getAllMembersInjectorInjectionPoints();
    }
}
