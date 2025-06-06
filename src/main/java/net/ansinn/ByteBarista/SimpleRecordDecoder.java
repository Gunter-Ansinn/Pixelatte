package net.ansinn.ByteBarista;
import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.ByteBarista.annotations.UnsignedShort;

import java.lang.annotation.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Standard record encoding can only be used on fixed sized variables. Variable variable sizes will have their own methods.
 */
public final class SimpleRecordDecoder {

    // Same goes for class instantiation methods
    private static final Map<Class<? extends Record>, Function<ByteBuffer, Object>[]> DeserializationCache = new ConcurrentHashMap<>();

    private SimpleRecordDecoder() {}

    /**
     * Decode a record from a bytebuffer into a new instance of type <T>. The default order is BIG ENDIAN when calling this method.
     * @param buffer to decode
     * @param recordClazz record type to decode to
     * @return new record instance
     * @param <T> type of record to return
     * @throws IllegalAccessException thrown when class access isn't permitted
     */
    public static <T extends Record> T decodeRecord(final ByteBuffer buffer, final Class<T> recordClazz) throws IllegalAccessException, NoSuchMethodException {
        return decodeRecord(buffer, recordClazz, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Decode a record from a bytebuffer into a new instance of type <T>.
     * @param buffer to decode
     * @param recordClazz record type to decode to
     * @param order the byte order to be used when decoding a record
     * @return new record instance
     * @param <T> type of record to return
     * @throws IllegalAccessException thrown when class access isn't permitted
     */
    public static <T extends Record> T decodeRecord(final ByteBuffer buffer, final Class<T> recordClazz, final ByteOrder order) throws IllegalAccessException, NoSuchMethodException {
        buffer.order(order);

        var recordSize = ClassUtils.getRecordSize(recordClazz);

        if (recordSize > buffer.remaining())
            throw new IllegalStateException("Not enough room in byte buffer for following record: " + recordClazz.getTypeName());

        var recordConstructor = getConstructor(recordClazz);
        var deserializers = getDeserializer(recordClazz);

        var parameters = Arrays.stream(deserializers).map(thing -> thing.apply(buffer)).toArray();

        try {
            var spreader = recordConstructor.asSpreader(Object[].class, parameters.length);
            //noinspection unchecked
            return (T) spreader.invoke(parameters);
        } catch (RuntimeException | Error exception) {
            throw exception;
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to instantiate record: " + recordClazz.getTypeName(), throwable);
        }
    }

    /**
     * Get or cache the deserialization calls needed for record assembly
     * @param recordClazz record to deserialize
     * @return array of functions which can deserialize the record
     */
    static Function<ByteBuffer, Object>[] getDeserializer(Class<? extends Record> recordClazz) {
        DeserializationCache.computeIfAbsent(recordClazz, _ -> cacheCalls(recordClazz));
        return DeserializationCache.get(recordClazz);
    }

    /**
     * Cache method calls used to put together records from flat bytebuffer layouts
     * @param recordClazz record type to parse out
     * @return array of functions needed to create the object from scratch in order.
     * @param <T> type of record
     */
    @SuppressWarnings("unchecked")
    private static <T extends Record> Function<ByteBuffer, Object>[] cacheCalls(final Class<T> recordClazz) {
        return Arrays.stream(recordClazz.getRecordComponents()).map(recordComponent -> {
            var componentType = recordComponent.getType();

            if (componentType.isEnum())
                return (Function<ByteBuffer, Object>) buffer -> componentType.getEnumConstants()[buffer.get()];
            else return switch (componentType.getTypeName()) {
                case "long" -> {
                    if(recordComponent.isAnnotationPresent(UnsignedByte.class))
                        yield (Function<ByteBuffer, Long>) NumericHelpers::getUnsignedByteAsLong;
                    else if(recordComponent.isAnnotationPresent(UnsignedShort.class))
                        yield (Function<ByteBuffer, Long>) NumericHelpers::getUnsignedShortAsLong;
                    else if(recordComponent.isAnnotationPresent(UnsignedInteger.class))
                        yield (Function<ByteBuffer, Long>) NumericHelpers::getUnsignedInt;
                    yield (Function<ByteBuffer, Long>)ByteBuffer::getLong;
                }
                case "int" -> {
                    if(recordComponent.isAnnotationPresent(UnsignedByte.class))
                        yield (Function<ByteBuffer, Integer>) NumericHelpers::getUnsignedByteAsInt;
                    else if(recordComponent.isAnnotationPresent(UnsignedShort.class))
                        yield (Function<ByteBuffer, Integer>) NumericHelpers::getUnsignedShortAsInt;
                    else if(recordComponent.isAnnotationPresent(UnsignedInteger.class))
                        throw buildSignException(UnsignedInteger.class, recordClazz, "int", "long");
                    yield (Function<ByteBuffer, Integer>)ByteBuffer::getInt;
                }
                case "short" -> (Function<ByteBuffer, Short>)ByteBuffer::getShort;
                case "byte" -> (Function<ByteBuffer, Byte>)ByteBuffer::get;

                case "double" -> (Function<ByteBuffer, Double>)ByteBuffer::getDouble;
                case "float" -> (Function<ByteBuffer, Float>)ByteBuffer::getFloat;

                case "char" -> (Function<ByteBuffer, Character>)ByteBuffer::getChar;

                default -> throw new IllegalStateException("Unexpected type: " + componentType.getTypeName());
            };
        }).toArray(Function[]::new);
    }

    /**
     * Get record constructor with only primitive and or enumerated types
     * @param recordClazz record to get constructor for
     * @return typed constructor of record
     * @param <T> type of record
     */
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

    // TODO: Absolute waste of a method tbh. Expand this later.
    private static IllegalStateException buildSignException(Class<? extends Annotation> annotation, Class<? extends Record> clazz, String intended, String suggested) {
        return new IllegalStateException("Invalid use of " + annotation.getTypeName() + " annotation on int in: " + clazz.getTypeName() + ". If you mean to use an unsigned " + intended + " change this type to " + suggested + ".");
    }

}

