package li.earth.urchin.twic.sarcoptic;

import org.objectweb.asm.Opcodes;

enum Kind {
    BOOLEAN(boolean.class, 'Z', Opcodes.IRETURN, Opcodes.ICONST_0),
    BYTE(byte.class, 'B', Opcodes.IRETURN, Opcodes.ICONST_0),
    SHORT(short.class, 'S', Opcodes.IRETURN, Opcodes.ICONST_0),
    INT(int.class, 'I', Opcodes.IRETURN, Opcodes.ICONST_0),
    LONG(long.class, 'J', Opcodes.LRETURN, Opcodes.LCONST_0),
    CHAR(char.class, 'C', Opcodes.IRETURN, Opcodes.ICONST_0),
    FLOAT(float.class, 'F', Opcodes.FRETURN, Opcodes.FCONST_0),
    DOUBLE(double.class, 'D', Opcodes.DRETURN, Opcodes.DCONST_0),
    OBJECT(Object.class, 'L', Opcodes.ARETURN, Opcodes.ACONST_NULL) {
        @Override
        public String descriptor(Class type) {
            return this.descriptorSymbol + ClassFileUtils.binaryName(type) + ";";
        }
    };

    public static Kind of(Class type) {
        if (type.isPrimitive()) {
            for (Kind kind : values()) {
                if (kind.type.equals(type)) return kind;
            }
            throw new AssertionError("unknown primitive type: " + type);
        } else {
            return OBJECT;
        }
    }

    public final Class<?> type;
    protected final char descriptorSymbol;
    public final int returnOpcode;
    public final int zeroOpcode;

    Kind(Class<?> type, char descriptorSymbol, int returnOpcode, int zeroOpcode) {
        this.type = type;
        this.descriptorSymbol = descriptorSymbol;
        this.returnOpcode = returnOpcode;
        this.zeroOpcode = zeroOpcode;
    }

    public String descriptor(Class type) {
        return String.valueOf(descriptorSymbol);
    }

}
