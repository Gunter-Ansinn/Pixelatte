package net.ansinn.ByteBarista;

import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.ByteBarista.annotations.UnsignedShort;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DynamicRecordDecoder {

    static <T extends Record> MethodHandle buildDecoder(Class<T> recordClazz, MethodHandles.Lookup lookup) throws NoSuchMethodException, IllegalAccessException {
        var components = recordClazz.getRecordComponents();

        var deserializers = new MethodHandle[components.length];
        for (var i = 0; i < components.length; i++) {
            deserializers[i] = makeDeserializer(components[i]);
        }

        return MethodHandles.filterArguments(getConstructor(recordClazz), 0, deserializers);
    }

    private static MethodHandle makeDeserializer(RecordComponent component) throws NoSuchMethodException, IllegalAccessException {
        var type = component.getType();
        var lookup = MethodHandles.lookup();

        if (type.isEnum()) {
            return MethodHandles.insertArguments(
                    lookup.findStatic(type, "values", MethodType.methodType(type.arrayType())),
                    0
            );
        }

        return switch (type.getTypeName().toLowerCase()) {
            case "long" -> {
                if (component.isAnnotationPresent(UnsignedByte.class))
                    yield MethodHandles.lookup().findStatic(SimpleRecordDecoder.class, "getUnsignedByteAsLong", MethodType.methodType(long.class, ByteBuffer.class));
                else if (component.isAnnotationPresent(UnsignedShort.class))
                    yield MethodHandles.lookup().findStatic(SimpleRecordDecoder.class, "getUnsignedShortAsLong", MethodType.methodType(long.class, ByteBuffer.class));
                else if (component.isAnnotationPresent(UnsignedInteger.class))
                    yield MethodHandles.lookup().findStatic(SimpleRecordDecoder.class, "getUnsignedInt", MethodType.methodType(long.class, ByteBuffer.class));
                yield MethodHandles.lookup().findVirtual(ByteBuffer.class, "getLong", MethodType.methodType(long.class));
            }
            case "int" -> {
                if (component.isAnnotationPresent(UnsignedByte.class))
                    yield MethodHandles.lookup().findStatic(SimpleRecordDecoder.class, "getUnsignedByteAsInt", MethodType.methodType(int.class, ByteBuffer.class));
                else if (component.isAnnotationPresent(UnsignedShort.class))
                    yield MethodHandles.lookup().findStatic(SimpleRecordDecoder.class, "getUnsignedShortAsInt", MethodType.methodType(int.class, ByteBuffer.class));
                yield MethodHandles.lookup().findVirtual(ByteBuffer.class, "getInt", MethodType.methodType(int.class));
            }
            case "short" ->
                    MethodHandles.lookup().findVirtual(ByteBuffer.class, "getShort", MethodType.methodType(short.class));
            case "byte" ->
                    MethodHandles.lookup().findVirtual(ByteBuffer.class, "get", MethodType.methodType(byte.class));

            case "double" ->
                    MethodHandles.lookup().findVirtual(ByteBuffer.class, "getDouble", MethodType.methodType(double.class));
            case "float" ->
                    MethodHandles.lookup().findVirtual(ByteBuffer.class, "getFloat", MethodType.methodType(float.class));

            case "char" ->
                    MethodHandles.lookup().findVirtual(ByteBuffer.class, "getChar", MethodType.methodType(char.class));
            default -> throw new IllegalStateException("Unexpected type: " + type.getTypeName());
        };
    }

    private static <T extends Record> MethodHandle getConstructor(final Class<T> recordClazz) throws NoSuchMethodException, IllegalAccessException {

        var lookup = MethodHandles.lookup();

        // Get record components
        var components = recordClazz.getRecordComponents();

        // Get types of record fields (primitives or enums only)
        Class<?>[] paramTypes = Arrays.stream(components)
                .map(RecordComponent::getType)
                .filter(type -> type.isPrimitive() || type.isEnum()) // Filter out non-primitive/enum types
                .toArray(Class<?>[]::new);

        if (paramTypes.length != components.length) {
            throw new IllegalStateException("The following record: " + recordClazz.getTypeName() +
                    ", has non-primitive or non-enum fields, which are not allowed.");
        }

        // Find the canonical constructor using method handles
        return lookup.findConstructor(recordClazz, MethodType.methodType(void.class, paramTypes));

    }
}
