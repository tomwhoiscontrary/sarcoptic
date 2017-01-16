package li.earth.urchin.twic.sarcoptic;

class ClassFileUtils {

    static String binaryName(Class<?> type) {
        return binaryName(type.getName());
    }

    static String binaryName(String className) {
        return className.replace('.', '/');
    }

}
