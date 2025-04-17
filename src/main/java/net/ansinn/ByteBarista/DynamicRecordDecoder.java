package net.ansinn.ByteBarista;

import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.ByteBarista.annotations.UnsignedShort;

import java.lang.invoke.*;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Function;

public class DynamicRecordDecoder {

    public static <T extends Record> Function<ByteBuffer, T> createDecoder(Class<T> recordClazz) throws NoSuchMethodException, IllegalAccessException, LambdaConversionException {
        var lookup = MethodHandles.privateLookupIn(recordClazz, MethodHandles.lookup());
        var decoder = buildDecoder(recordClazz, lookup);



        return null;
    }

    public static <T extends Record> MethodHandle buildDecoder(Class<T> recordClazz, MethodHandles.Lookup lookup) throws NoSuchMethodException, IllegalAccessException {
        var components = recordClazz.getRecordComponents();
//        var constructor = getConstructor(recordClazz, lookup);
        var tempConstructor = recordClazz.getDeclaredConstructors()[0];
        var constructor = lookup.unreflectConstructor(tempConstructor);

        var deserializers = new MethodHandle[components.length];

        for (var i = 0; i < components.length; i++) {
            var deserializer = makeDeserializer(components[i], lookup);
            System.out.println("deserializer = " + deserializer);
            deserializers[i] = deserializer;
        }

        var bufferedReader = MethodHandles.zero(recordClazz);

        for (var reader : deserializers) {
            bufferedReader = MethodHandles.collectArguments(bufferedReader, 0, reader);
        }

        return MethodHandles.foldArguments(constructor, bufferedReader);
    }

    private static MethodHandle makeDeserializer(RecordComponent component, MethodHandles.Lookup lookup) throws NoSuchMethodException, IllegalAccessException {
        var type = component.getType();

        if (type.isEnum()) {
            var valueHandle = lookup.findStatic(type, "values", MethodType.methodType(type.arrayType()));
            var ordinal = lookup.findVirtual(ByteBuffer.class, "getInt", MethodType.methodType(int.class));

            return MethodHandles.filterReturnValue(ordinal, valueHandle);
        }

        return switch (type.getTypeName().toLowerCase()) {
            case "long" -> {
                if (component.isAnnotationPresent(UnsignedByte.class))
                    yield lookup.findStatic(NumericHelpers.class, "getUnsignedByteAsLong", MethodType.methodType(long.class, ByteBuffer.class));
                else if (component.isAnnotationPresent(UnsignedShort.class))
                    yield lookup.findStatic(NumericHelpers.class, "getUnsignedShortAsLong", MethodType.methodType(long.class, ByteBuffer.class));
                else if (component.isAnnotationPresent(UnsignedInteger.class))
                    yield lookup.findStatic(NumericHelpers.class, "getUnsignedInt", MethodType.methodType(long.class, ByteBuffer.class));
                yield lookup.findVirtual(ByteBuffer.class, "getLong", MethodType.methodType(long.class));
            }
            case "int" -> {
                if (component.isAnnotationPresent(UnsignedByte.class))
                    yield lookup.findStatic(NumericHelpers.class, "getUnsignedByteAsInt", MethodType.methodType(int.class, ByteBuffer.class));
                else if (component.isAnnotationPresent(UnsignedShort.class))
                    yield lookup.findStatic(NumericHelpers.class, "getUnsignedShortAsInt", MethodType.methodType(int.class, ByteBuffer.class));
                yield lookup.findVirtual(ByteBuffer.class, "getInt", MethodType.methodType(int.class));
            }
            case "short" ->
                    lookup.findVirtual(ByteBuffer.class, "getShort", MethodType.methodType(short.class));
            case "byte" ->
                    lookup.findVirtual(ByteBuffer.class, "get", MethodType.methodType(byte.class));

            case "double" ->
                    lookup.findVirtual(ByteBuffer.class, "getDouble", MethodType.methodType(double.class));
            case "float" ->
                    lookup.findVirtual(ByteBuffer.class, "getFloat", MethodType.methodType(float.class));

            case "char" ->
                    lookup.findVirtual(ByteBuffer.class, "getChar", MethodType.methodType(char.class));
            default -> throw new IllegalStateException("Unexpected type: " + type.getTypeName());
        };
    }

    private static <T extends Record> MethodHandle getConstructor(final Class<T> recordClazz, MethodHandles.Lookup lookup) throws NoSuchMethodException, IllegalAccessException {
        // Get record components
        var components = recordClazz.getRecordComponents();

        // Get types of record fields (primitives or enums only)
        var paramTypes = Arrays.stream(components)
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
