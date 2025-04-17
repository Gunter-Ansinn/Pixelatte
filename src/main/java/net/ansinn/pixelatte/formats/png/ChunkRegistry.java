package net.ansinn.pixelatte.formats.png;

import net.ansinn.ByteBarista.SimpleRecordDecoder;
import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.RawChunk;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;
import net.ansinn.pixelatte.formats.png.layout.chunks.gAMA;
import net.ansinn.pixelatte.formats.png.layout.chunks.tRNS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ChunkRegistry {

    private static final Map<Integer, Registry> Decoders = new HashMap<>();

    static {
        register("IHDR", IHDR.class);
        register("gAMA", gAMA.class);
    }

    public static <T extends Record & Chunk> void register(String chunkName, Class<T> chunkClazz) {
        var key = toTag(chunkName);
        Decoders.put(key, new Registry.Automatic(chunkClazz));
    }

    public static <T extends Record & Chunk> void register(String chunkName, Function<ByteBuffer, Chunk> parsingFunction) {
        var key = toTag(chunkName);
        Decoders.put(key, new Registry.Deferred(parsingFunction));
    }

    public static boolean isRegistered(String name) {
        return isRegistered(toTag(name));
    }

    public static boolean isRegistered(int tag) {
        return Decoders.containsKey(tag);
    }

    @SuppressWarnings("unchecked")
    public static Chunk decodeChunk(int key, byte[] data, long crc) throws IllegalAccessException, NoSuchMethodException {
        var decoder = getDecoder(key);

        return switch (decoder) {
            case Registry.Automatic(var clazz) ->
                    (Chunk) SimpleRecordDecoder.decodeRecord(ByteBuffer.wrap(data), (Class<? extends Record>)clazz);
            case Registry.Deferred(var decodeFunc) ->
                    decodeFunc.apply(ByteBuffer.wrap(data));
            case Registry.None ignored -> {
                System.err.println("Unregistered chunk type with id: " + key);
                yield new RawChunk(key, data, 0);
            }
        };
    }

    public static Registry getDecoder(String chunkName) {
        return getDecoder(toTag(chunkName));
    }

    public static Registry getDecoder(int key) {
        return Decoders.getOrDefault(key, new Registry.None());
    }

    public static Integer toTag(String name) {
        return ByteBuffer.wrap(name.getBytes(StandardCharsets.US_ASCII)).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    public sealed interface Registry {
        record Deferred(Function<ByteBuffer, Chunk> parser) implements Registry {}
        record Automatic(Class<? extends Chunk> clazz) implements Registry {}
        record None() implements Registry {}
    }

}
