package net.ansinn.ByteBarista;

import java.nio.ByteBuffer;

public class NumericHelpers {
    static long getUnsignedInt(ByteBuffer buffer) {
        var num = buffer.getInt();
        return Integer.toUnsignedLong(num);
    }

    static long getUnsignedShortAsLong(ByteBuffer buffer) {
        var num = buffer.getShort();
        return Short.toUnsignedLong(num);
    }

    static long getUnsignedByteAsLong(ByteBuffer buffer) {
        var num = buffer.get();
        return Byte.toUnsignedLong(num);
    }

    static int getUnsignedShortAsInt(ByteBuffer buffer) {
        var num = buffer.getShort();
        return Short.toUnsignedInt(num);
    }

    static int getUnsignedByteAsInt(ByteBuffer buffer) {
        var num = buffer.get();
        return Byte.toUnsignedInt(num);
    }
}
