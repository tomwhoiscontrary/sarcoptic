package li.earth.urchin.twic.sarcoptic;

import org.objectweb.asm.Opcodes;

enum Kind {
    BOOLEAN(boolean.class, 'Z', Opcodes.IRETURN, 1),
    BYTE(byte.class, 'B', Opcodes.IRETURN, 1),
    SHORT(short.class, 'S', Opcodes.IRETURN, 1),
    INT(int.class, 'I', Opcodes.IRETURN, 1),
    LONG(long.class, 'J', Opcodes.LRETURN, 2),
    CHAR(char.class, 'C', Opcodes.IRETURN, 1),
    FLOAT(float.class, 'F', Opcodes.FRETURN, 1),
    DOUBLE(double.class, 'D', Opcodes.DRETURN, 2),
    OBJECT(Object.class, 'L', Opcodes.ARETURN, 1) {
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
    public final int stackSize;

    Kind(Class<?> type, char descriptorSymbol, int returnOpcode, int stackSize) {
        this.type = type;
        this.descriptorSymbol = descriptorSymbol;
        this.returnOpcode = returnOpcode;
        this.stackSize = stackSize;
    }

    public String descriptor(Class type) {
        return String.valueOf(descriptorSymbol);
    }

}
