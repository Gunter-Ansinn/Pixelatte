package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.ChunkError;

import java.nio.ByteBuffer;

import static net.ansinn.pixelatte.formats.png.ChunkRegistry.toTag;

public sealed interface sBIT extends Chunk {

    int TAG = toTag("sBIT");

    static Chunk provider(ByteBuffer data, IHDR header) {
        return switch (header.colorType()) {
            case Grayscale -> new Grayscale(data.get());
            case TrueColor, Indexed -> new TrueColor(data.get(), data.get(), data.get());
            case GreyscaleAlpha -> new GrayscaleAlpha(data.get(), data.get());
            case TrueColorAlpha -> new TrueColorAlpha(data.get(), data.get(), data.get(), data.get());
            default ->
                    new ChunkError(TAG, data.array(), "sBIT invalid for color type: " + header.colorType());
        };
    }

    record Grayscale(byte gray) implements sBIT {}
    record TrueColor(byte red, byte green, byte blue) implements sBIT {}
    record GrayscaleAlpha(byte gray, byte alpha) implements sBIT {}
    record TrueColorAlpha(byte red, byte green, byte blue, byte alpha) implements sBIT {}
}