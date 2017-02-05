package li.earth.urchin.twic.sarcoptic;

import org.objectweb.asm.Opcodes;

enum Kind {
    BOOLEAN(boolean.class, 'Z', Opcodes.ICONST_0, Opcodes.ILOAD, Opcodes.IRETURN),
    BYTE(byte.class, 'B', Opcodes.ICONST_0, Opcodes.ILOAD, Opcodes.IRETURN),
    SHORT(short.class, 'S', Opcodes.ICONST_0, Opcodes.ILOAD, Opcodes.IRETURN),
    INT(int.class, 'I', Opcodes.ICONST_0, Opcodes.ILOAD, Opcodes.IRETURN),
    LONG(long.class, 'J', Opcodes.LCONST_0, Opcodes.LLOAD, Opcodes.LRETURN),
    CHAR(char.class, 'C', Opcodes.ICONST_0, Opcodes.ILOAD, Opcodes.IRETURN),
    FLOAT(float.class, 'F', Opcodes.FCONST_0, Opcodes.FLOAD, Opcodes.FRETURN),
    DOUBLE(double.class, 'D', Opcodes.DCONST_0, Opcodes.DLOAD, Opcodes.DRETURN),
    OBJECT(Object.class, 'L', Opcodes.ACONST_NULL, Opcodes.ALOAD, Opcodes.ARETURN) {
        @Override
        public String descriptor(Class type) {
            return this.descriptorSymbol + ClassFileUtils.binaryName(type) + ";";
        }
    };

    public static Kind of(Class<?> type) {
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
    public final int zeroOpcode;
    public final int loadOpcode;
    public final int returnOpcode;

    Kind(Class<?> type, char descriptorSymbol, int zeroOpcode, int loadOpcode, int returnOpcode) {
        this.type = type;
        this.descriptorSymbol = descriptorSymbol;
        this.zeroOpcode = zeroOpcode;
        this.loadOpcode = loadOpcode;
        this.returnOpcode = returnOpcode;
    }

    public String descriptor(Class<?> type) {
        return String.valueOf(descriptorSymbol);
    }

}
