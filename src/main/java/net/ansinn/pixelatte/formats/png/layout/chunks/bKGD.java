package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.ChunkError;

import java.nio.ByteBuffer;

import static net.ansinn.pixelatte.formats.png.ChunkRegistry.toTag;

public sealed interface bKGD extends Chunk {

    int TAG = toTag("bKGD");

    static Chunk provider(ByteBuffer data, IHDR header) {
        return switch (header.colorType()) {
            case Grayscale, GreyscaleAlpha -> {
                if (data.remaining() < Short.BYTES) {
                    System.err.println("bKGD too short for Grayscale; expected two bytes, found: " + data.remaining());
                    yield new ChunkError(TAG, data.array(), "bKGD too short for Grayscale; expected two bytes, found: " + data.remaining());
                }
                yield new GrayScale(data.getShort());
            }
            case TrueColor, TrueColorAlpha -> new TrueColor(data.getShort(), data.getShort(), data.getShort());
            default ->
                    new ChunkError(TAG, data.array(), "bKGD is an invalid chunk for color type: " + header.colorType());
        };
    }

    record GrayScale(short grayscale) implements bKGD {

    }

    record TrueColor(short red, short green, short blue) implements bKGD {

    }

}
