package li.earth.urchin.twic.sarcoptic;

public interface Struct<T extends Struct<T>> {

    public static <T extends Struct<T>> T of(Class<T> type) {
        Class<? extends T> implementationClass = StructImplementationRegistry.INSTANCE.getImplementation(type);

        try {
            return implementationClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AssertionError("impossible exception thrown while instantiating " + implementationClass, e);
        }
    }

}
