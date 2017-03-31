package li.earth.urchin.twic.sarcoptic;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class StructImplementationRegistry extends ClassLoader {

    public static final StructImplementationRegistry INSTANCE = new StructImplementationRegistry();

    private final Map<Class<?>, Class<?>> cache = new ConcurrentHashMap<>();

    private StructImplementationRegistry() {}

    public <T extends Struct<T>> Class<? extends T> getImplementation(Class<T> type) {
        return cache.computeIfAbsent(type, t -> makeImplementation(check(t).asSubclass(Struct.class))).asSubclass(type);
    }

    private Class<?> check(Class<?> ifaceType) {
        if (ifaceType == null) throw new NullPointerException();
        if (!ifaceType.isInterface()) throw new IllegalArgumentException(ifaceType + " is not an interface");
        if (ifaceType.equals(Struct.class) || !Struct.class.isAssignableFrom(ifaceType)) {
            throw new ClassCastException(ifaceType + " is not a proper subtype of Struct");
        }
        return ifaceType;
    }

    private <T extends Struct<T>> Class<? extends T> makeImplementation(Class<T> ifaceType) {
        String implClassName = ifaceType.getName() + "Impl";
        Map<String, Class<?>> properties = Arrays.stream(ifaceType.getDeclaredMethods())
                .sorted(Comparator.comparing(Method::getName))
                .collect(Collectors.toMap(Method::getName, Method::getReturnType, this::noMerges, LinkedHashMap::new));

        ClassWriter classWriter = makeClass(implClassName, StructImpl.class, ifaceType);
        makeBindingConstructor(classWriter, implClassName, StructImpl.class, properties);
        makeNullaryConstructor(classWriter, implClassName, StructImpl.class, properties);
        for (Map.Entry<String, Class<?>> property : properties.entrySet()) {
            makeProperty(classWriter, implClassName, property.getKey(), property.getValue(), properties);
        }

        byte[] implClassBytes = classWriter.toByteArray();
        return defineClass(implClassName, implClassBytes, 0, implClassBytes.length).asSubclass(ifaceType);
    }

    private <T> T noMerges(T a, T b) {
        throw new IllegalStateException();
    }

    private ClassWriter makeClass(String implClassName, Class<?> baseType, Class<?> ifaceType) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classWriter.visit(Opcodes.V1_8,
                          Opcodes.ACC_SUPER | Opcodes.ACC_PUBLIC,
                          ClassFileUtils.binaryName(implClassName),
                          signature(baseType, ifaceType),
                          ClassFileUtils.binaryName(baseType),
                          new String[]{ClassFileUtils.binaryName(ifaceType)});
        return classWriter;
    }

    private void makeBindingConstructor(ClassWriter classWriter, String implClassName, Class<StructImpl> baseType, Map<String, Class<?>> properties) {
        MethodVisitor ctor = classWriter.visitMethod(Opcodes.ACC_PUBLIC,
                                                     "<init>",
                                                     constructorDescriptor(properties.values()),
                                                     null,
                                                     null);
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, ClassFileUtils.binaryName(baseType), "<init>", "()V", false);

        int index = 1;
        for (Map.Entry<String, Class<?>> property : properties.entrySet()) {
            String propName = property.getKey();
            Class<?> propType = property.getValue();
            Kind propKind = Kind.of(propType);

            ctor.visitVarInsn(Opcodes.ALOAD, 0);
            ctor.visitVarInsn(propKind.loadOpcode, index);
            ctor.visitFieldInsn(Opcodes.PUTFIELD, ClassFileUtils.binaryName(implClassName), propName, propKind.descriptor(propType));

            index += Type.getType(propType).getSize();
        }

        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();
    }

    private void makeNullaryConstructor(ClassWriter classWriter, String implClassName, Class<StructImpl> baseType, Map<String, Class<?>> properties) {
        MethodVisitor ctor = classWriter.visitMethod(Opcodes.ACC_PUBLIC,
                                                     "<init>",
                                                     "()V",
                                                     null,
                                                     null);
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);

        for (Map.Entry<String, Class<?>> property : properties.entrySet()) {
            Class<?> propType = property.getValue();
            Kind propKind = Kind.of(propType);

            ctor.visitInsn(propKind.zeroOpcode);
        }

        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, ClassFileUtils.binaryName(implClassName), "<init>", constructorDescriptor(properties.values()), false);
        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();
    }

    private String constructorDescriptor(Collection<Class<?>> parameterTypes) {
        return parameterTypes.stream()
                .map(c -> Kind.of(c).descriptor(c))
                .collect(Collectors.joining("", "(", ")V"));
    }

    private void makeProperty(ClassWriter classWriter, String implClassName, String propName, Class<?> propType, Map<String, Class<?>> properties) {
        Kind propKind = Kind.of(propType);
        String propDescriptor = propKind.descriptor(propType);

        makePropertyField(classWriter, propName, propDescriptor);
        makePropertyAccessor(classWriter, implClassName, propName, propKind, propDescriptor);
        makePropertySetter(implClassName, classWriter, propName, propKind, propDescriptor, properties);
    }

    private void makePropertyField(ClassWriter classWriter, String propName, String propDescriptor) {
        FieldVisitor field = classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                                                    propName,
                                                    propDescriptor,
                                                    null,
                                                    null);
        field.visitEnd();
    }

    private void makePropertyAccessor(ClassWriter classWriter, String implClassName, String propName, Kind propKind, String propDescriptor) {
        MethodVisitor accessor = classWriter.visitMethod(Opcodes.ACC_PUBLIC,
                                                         propName,
                                                         "()" + propDescriptor,
                                                         null,
                                                         null);
        accessor.visitCode();
        accessor.visitVarInsn(Opcodes.ALOAD, 0);
        accessor.visitFieldInsn(Opcodes.GETFIELD, ClassFileUtils.binaryName(implClassName), propName, propDescriptor);
        accessor.visitInsn(propKind.returnOpcode);
        accessor.visitMaxs(0, 0);
        accessor.visitEnd();
    }

    private void makePropertySetter(String implClassName, ClassWriter classWriter, String propName, Kind propKind, String propDescriptor, Map<String, Class<?>> properties) {
        MethodVisitor setter = classWriter.visitMethod(Opcodes.ACC_PUBLIC,
                                                       "with" + capitalise(propName),
                                                       "(" + propDescriptor + ")L" + ClassFileUtils.binaryName(implClassName) + ";",
                                                       null,
                                                       null);
        setter.visitCode();
        setter.visitTypeInsn(Opcodes.NEW, ClassFileUtils.binaryName(implClassName));
        setter.visitInsn(Opcodes.DUP);
        properties.forEach((n, c) -> {
            if (n.equals(propName)) {
                setter.visitVarInsn(propKind.loadOpcode, 1);
            } else {
                setter.visitVarInsn(Opcodes.ALOAD, 0);
                setter.visitFieldInsn(Opcodes.GETFIELD, ClassFileUtils.binaryName(implClassName), n, Kind.of(c).descriptor(c));
            }
        });
        setter.visitMethodInsn(Opcodes.INVOKESPECIAL, ClassFileUtils.binaryName(implClassName), "<init>", constructorDescriptor(properties.values()), false);
        setter.visitInsn(Opcodes.ARETURN);
        setter.visitMaxs(0, 0);
        setter.visitEnd();
    }

    private String capitalise(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String signature(Class<?> baseType, Class<?> ifaceType) {
        String baseBinaryName = ClassFileUtils.binaryName(baseType);
        String ifaceDescriptor = Kind.OBJECT.descriptor(ifaceType);
        return "L" + baseBinaryName + "<" + ifaceDescriptor + ">" + ";" + ifaceDescriptor;
    }

}
