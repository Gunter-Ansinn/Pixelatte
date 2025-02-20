package net.ansinn.ByteBarista;

import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.ByteBarista.annotations.UnsignedShort;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassUtils {
    // We're going to want to cache commonly used record sizes to cut down on needless sum calls.
    private static final Map<Class<? extends Record>, Integer> SizeCache = new ConcurrentHashMap<>();

    /**
     * Get cumulative size of types within record in bytes from cache or calculate it anew.
     *
     * @param recordClazz record to read
     * @return number of bytes in record
     */
    public static int getRecordSize(final Class<? extends Record> recordClazz) {
        SizeCache.computeIfAbsent(recordClazz, _ -> sumFieldSizes(recordClazz.getRecordComponents()));
        return SizeCache.get(recordClazz);
    }

    /**
     * Sum sizes of fixed sized variables to assist in faster computation of variables.
     * Enums *are* also allowed on the condition that there's less than 255 enum ordinals.
     *
     * @param components component fields to be summed up
     * @return size of object fields
     */
    private static int sumFieldSizes(final RecordComponent[] components) {
        return Arrays.stream(components).mapToInt(field -> field.getType().isEnum()
                ? Byte.BYTES : switch (field.getType().getTypeName()) {
            case "long" -> {
                if (field.isAnnotationPresent(UnsignedByte.class))
                    yield Byte.BYTES;
                else if (field.isAnnotationPresent(UnsignedShort.class))
                    yield Short.BYTES;
                else if (field.isAnnotationPresent(UnsignedInteger.class))
                    yield Integer.BYTES;
                yield Long.BYTES;
            }
            case "int" -> {
                if (field.isAnnotationPresent(UnsignedByte.class))
                    yield Byte.BYTES;
                else if (field.isAnnotationPresent(UnsignedShort.class))
                    yield Short.BYTES;
                yield Integer.BYTES;
            }
            case "short" -> Short.BYTES;
            case "byte" -> Byte.BYTES;

            case "double" -> Double.BYTES;
            case "float" -> Float.BYTES;

            case "char" -> Character.BYTES;

            case "boolean" -> throw new IllegalStateException("Type 'Boolean' is not a permitted value");
            default -> throw new IllegalStateException("Unexpected value: " + field.getType().getTypeName());
        }).sum();
    }
}
