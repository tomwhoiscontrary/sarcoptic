package li.earth.urchin.twic.sarcoptic;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.util.Arrays;
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
                .collect(Collectors.toMap(Method::getName, Method::getReturnType));

        ClassWriter classWriter = makeClass(implClassName, StructImpl.class, ifaceType);
        makeNullaryConstructor(classWriter, implClassName, StructImpl.class, properties);
        for (Map.Entry<String, Class<?>> property : properties.entrySet()) {
            makeProperty(classWriter, implClassName, property.getKey(), property.getValue());
        }

        byte[] implClassBytes = classWriter.toByteArray();
        return defineClass(implClassName, implClassBytes, 0, implClassBytes.length).asSubclass(ifaceType);
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

    private void makeNullaryConstructor(ClassWriter classWriter, String implClassName, Class<StructImpl> baseType, Map<String, Class<?>> properties) {
        MethodVisitor ctor = classWriter.visitMethod(Opcodes.ACC_PUBLIC,
                                                     "<init>",
                                                     "()V",
                                                     null,
                                                     null);
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, ClassFileUtils.binaryName(baseType), "<init>", "()V", false);

        for (Map.Entry<String, Class<?>> property : properties.entrySet()) {
            String propName = property.getKey();
            Class<?> propType = property.getValue();
            Kind propKind = Kind.of(propType);

            ctor.visitVarInsn(Opcodes.ALOAD, 0);
            ctor.visitInsn(propKind.zeroOpcode);
            ctor.visitFieldInsn(Opcodes.PUTFIELD, ClassFileUtils.binaryName(implClassName), propName, propKind.descriptor(propType));
        }

        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();
    }

    private void makeProperty(ClassWriter classWriter, String implClassName, String propName, Class<?> propType) {
        Kind propKind = Kind.of(propType);
        String propDescriptor = propKind.descriptor(propType);

        makePropertyField(classWriter, propName, propDescriptor);
        makePropertyAccessor(classWriter, implClassName, propName, propKind, propDescriptor);
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

    private String signature(Class<?> baseType, Class<?> ifaceType) {
        String baseBinaryName = ClassFileUtils.binaryName(baseType);
        String ifaceDescriptor = Kind.OBJECT.descriptor(ifaceType);
        return "L" + baseBinaryName + "<" + ifaceDescriptor + ">" + ";" + ifaceDescriptor;
    }

}
