package li.earth.urchin.twic.sarcoptic;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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

    @Test
    public void canCreateAStructImplementationWithPropertyValues() throws Exception {
        // this is pretty gross for now!
        Dog prototype = Struct.of(Dog.class);
        // note that properties are sorted alphabetically
        Constructor<? extends Dog> constructor = prototype.getClass()
                                                          .getConstructor(char.class,
                                                                          byte.class,
                                                                          boolean.class,
                                                                          short.class,
                                                                          int.class,
                                                                          String.class,
                                                                          float.class,
                                                                          long.class,
                                                                          double.class);
        Dog dog = constructor.newInstance('5', (byte) 1, true, (short) 2, 3, "Rover", 6.0f, 4L, 7.0d);

        assertThat(dog.name(), equalTo("Rover"));
        assertThat(dog.good(), equalTo(true));
        assertThat(dog.bark(), equalTo((byte) 1));
        assertThat(dog.leash(), equalTo((short) 2));
        assertThat(dog.legs(), equalTo(3));
        assertThat(dog.walk(), equalTo(4L));
        assertThat(dog.acter(), equalTo('5'));
        assertThat(dog.speed(), equalTo(6.0f));
        assertThat(dog.weight(), equalTo(7.0d));
    }

    @Test
    public void canCreateACopyOfAStructWithOneFieldChanged() throws Exception {
        // this is pretty gross for now!
        Dog prototype = Struct.of(Dog.class);
        Class<? extends Dog> implClass = prototype.getClass();
        // note that properties are sorted alphabetically
        Constructor<? extends Dog> constructor = implClass.getConstructor(char.class,
                                                                          byte.class,
                                                                          boolean.class,
                                                                          short.class,
                                                                          int.class,
                                                                          String.class,
                                                                          float.class,
                                                                          long.class,
                                                                          double.class);
        Dog template = constructor.newInstance('5', (byte) 1, true, (short) 2, 3, "Rover", 6.0f, 4L, 7.0d);

        // again, pretty gross for now!
        Method withName = implClass.getMethod("withName", String.class);
        Dog dog = (Dog) withName.invoke(template, "Fido");

        assertThat(dog.name(), equalTo("Fido"));
        assertThat(dog.good(), equalTo(true));
        assertThat(dog.bark(), equalTo((byte) 1));
        assertThat(dog.leash(), equalTo((short) 2));
        assertThat(dog.legs(), equalTo(3));
        assertThat(dog.walk(), equalTo(4L));
        assertThat(dog.acter(), equalTo('5'));
        assertThat(dog.speed(), equalTo(6.0f));
        assertThat(dog.weight(), equalTo(7.0d));
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
