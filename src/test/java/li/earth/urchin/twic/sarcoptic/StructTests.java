package li.earth.urchin.twic.sarcoptic;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class StructTests {

    public interface Dog extends Struct<Dog> {

        String name();

        boolean good();

        byte bark();

        short leash();

        int legs();

        long walk();

        char acter();

        float speed();

        double weight();

    }

    public abstract class BadDog implements Dog {}

    @Test
    public void canCreateAStructImplementation() throws Exception {
        Dog dog = Struct.of(Dog.class);

        assertThat(dog.name(), nullValue());
        assertThat(dog.good(), equalTo(false));
        assertThat(dog.bark(), equalTo((byte) 0));
        assertThat(dog.leash(), equalTo((short) 0));
        assertThat(dog.legs(), equalTo((int) 0));
        assertThat(dog.walk(), equalTo((long) 0));
        assertThat(dog.acter(), equalTo((char) 0));
        assertThat(dog.speed(), equalTo((float) 0));
        assertThat(dog.weight(), equalTo((double) 0));
    }

    @Test
    public void canNotCreateAnImplementationOfSomethingWhichIsNotAStruct() throws Exception {
        assertThrows(() -> Struct.of(launder(null)), NullPointerException.class);

        assertThrows(() -> Struct.of(launder(String.class)), IllegalArgumentException.class);
        assertThrows(() -> Struct.of(launder(BadDog.class)), IllegalArgumentException.class);

        assertThrows(() -> Struct.of(launder(Struct.class)), ClassCastException.class);
        assertThrows(() -> Struct.of(launder(Runnable.class)), ClassCastException.class);
    }

    private void assertThrows(Supplier<?> block, Class<? extends Throwable> exceptionType) {
        try {
            block.get();
        } catch (Throwable e) {
            if (!exceptionType.isInstance(e)) {
                try {
                    Assert.assertEquals(exceptionType, e.getClass());
                } catch (AssertionError ae) {
                    ae.initCause(e);
                    throw ae;
                }
            } else return;
        }
        throw new AssertionError("did not throw an exception");
    }

    private Class<Dog> launder(Class<?> badClass) {
        @SuppressWarnings("unchecked")
        Class<Dog> cleanClass = (Class) badClass;
        return cleanClass;
    }

}
