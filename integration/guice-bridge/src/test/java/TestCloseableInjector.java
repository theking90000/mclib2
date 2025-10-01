import be.theking90000.mclib2.inject.CloseableInjector;
import be.theking90000.mclib2.inject.CloseableInjectorImpl;
import be.theking90000.mclib2.inject.Disposable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TestCloseableInjector {

    @Test
    public void testCreate() {
        CloseableInjector injector = CloseableInjectorImpl.createInjector(Stage.PRODUCTION, Arrays.asList(
                binder -> binder.bind(S.class).toInstance(new S())
        ));

        A a1 = injector.getInstance(A.class);
        A a2 = injector.getInstance(A.class);
        B b1 = injector.getInstance(B.class);

        System.out.println(((CloseableInjectorImpl)injector).debugGraph());

        System.out.println("a1 before close=" + a1);

        injector.close(a1);
        System.out.println("a1 after close=" + a1);

        System.out.println("a2=" + a2);
        System.out.println("b1=" + b1);
        injector.close();

        System.out.println("a2 after close=" + a2);
        System.out.println("b1 after close=" + b1);
    }

    public static class S {
        private static int j = 0;
        private int i = 0;

        public S() {
            i = ++j;
            System.out.println("New S : " + this);
        }

        @Override
        public String toString() {
            return "S{" +
                    "i=" + i +
                    '}';
        }
    }

    public static class A {

        private static int j = 0;
        private int i = 0;
        private B b;
        private D d;

        @Inject
        public A(B b, D d) {
            i = ++j;
            System.out.println("New A : " + this);
            this.b = b;
            this.d = d;
        }

        @Override
        public String toString() {
            return "A{" +
                    "i=" + i +
                    ", " +
                    "b=" + b +
                    ", d=" + d +
                    '}';
        }
    }

    public static class B implements Disposable {

        private static int j = 0;
        private int i = 0;
        private boolean disposed;

        private C c;
        private D d;

        @Inject
        private D d2;

        @Inject
        public B(C c, D d) {
            i = ++j;
            System.out.println("New B : " + this);
            this.disposed = false;
            this.c = c;
            this.d = d;
        }

        @Override
        public void dispose() {
            System.out.println("Disposing B(i="+i+") : " + this);
            this.disposed = true;
        }

        @Override
        public String toString() {
            return "B{" +
                    "i=" + i +
                    ", c=" + c +
                    ", d=" + d +
                    ", d2=" + d2 +
                    ", disposed=" + disposed +
                    '}';
        }
    }

    public static class C {
        private static int j = 0;
        private int i = 0;

        public C() {
            i = ++j;
            System.out.println("New C : " + this);
        }

        @Override
        public String toString() {
            return "C{" +
                    "i=" + i +
                    '}';
        }
    }

    public static class D implements Disposable {
        private static int j = 0;
        private int i = 0;
        private boolean disposed;

        @Inject
        S s;

        public D() {
            i = ++j;
            disposed = false;
            System.out.println("New D : " + this);
        }

        @Override
        public void dispose() {
            System.out.println("Disposing D(i="+i+") : " + this);
            this.disposed = true;
        }

        @Override
        public String toString() {
            return "D{" +
                    "s=" + s +
                    ", " +
                    "i=" + i +
                    ", disposed=" + disposed +
                    '}';
        }
    }

}
