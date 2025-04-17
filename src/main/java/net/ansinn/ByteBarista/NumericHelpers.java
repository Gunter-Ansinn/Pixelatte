package net.ansinn.ByteBarista;

import java.nio.ByteBuffer;

/**
 * Simple set of methods to read numeric types not explicitly encoded within ByteBuffer.class
 */
public class NumericHelpers {

    public static long getUnsignedInt(ByteBuffer buffer) {
        var num = buffer.getInt();
        return Integer.toUnsignedLong(num);
    }

    public static long getUnsignedShortAsLong(ByteBuffer buffer) {
        var num = buffer.getShort();
        return Short.toUnsignedLong(num);
    }

    public static long getUnsignedByteAsLong(ByteBuffer buffer) {
        var num = buffer.get();
        return Byte.toUnsignedLong(num);
    }

    public static int getUnsignedShortAsInt(ByteBuffer buffer) {
        var num = buffer.getShort();
        return Short.toUnsignedInt(num);
    }

    public static int getUnsignedByteAsInt(ByteBuffer buffer) {
        var num = buffer.get();
        return Byte.toUnsignedInt(num);
    }
}
