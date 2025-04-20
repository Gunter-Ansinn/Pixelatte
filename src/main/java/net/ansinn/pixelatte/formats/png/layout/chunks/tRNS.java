package net.ansinn.pixelatte.formats.png.layout.chunks;


import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.RawChunk;

import java.nio.ByteBuffer;

import static net.ansinn.pixelatte.formats.png.ChunkRegistry.toTag;

public sealed interface tRNS extends Chunk {

    int TAG = toTag("tRNS");

    static Chunk provider(ByteBuffer data, IHDR header) {
        return switch (header.colorType()) {
            case TrueColor -> {
                if (data.remaining() < 6) {
                    System.err.print("tRNS chunk too short for TrueColor; expected six bytes, found: " + data.remaining());
                    yield new RawChunk(TAG, data.array(), 0);
                }

                var red = Short.toUnsignedInt(data.getShort());
                var green = Short.toUnsignedInt(data.getShort());
                var blue = Short.toUnsignedInt(data.getShort());
                yield new TrueColor(red, green, blue);
            }
            case Grayscale -> {
                if (data.remaining() < 2) {
                    System.err.println("tRNS chunk too short for Grayscale; expected 2 bytes, found: " + data.remaining());
                    yield new RawChunk(TAG, data.array(), 0);
                }

                var gray = Short.toUnsignedInt(data.getShort());
                yield new Grayscale(gray);
            }
            case Indexed -> new Indexed(data.array());
            default -> {
                if (!data.hasRemaining()) {
                    System.err.println("tRNS chunk has no alpha data for Indexed color.");
                    yield new RawChunk(TAG, data.array(), 0);
                }

                System.err.println("tRNS is an invalid chunk for color type: " + header.colorType());
                yield new RawChunk(TAG, data.array(), 0);
            }
        };
    }

    /**
     * Defines the red, green, and blue values which become transparent within an image.
     * @param red
     * @param green
     * @param blue
     */
    record TrueColor(int red, int green, int blue) implements tRNS {}

    /**
     * Holds a grayscale value which will be read as fully transparent.
     * @param grayValue the transparent gray.
     */
    record Grayscale(int grayValue) implements tRNS {}

    /**
     * represents a tRNS chunk variant for image type of indexed where the array corresponds to a palette index.
     * @param alpha indexed
     */
    record Indexed(byte[] alpha) implements tRNS {

        public Indexed {
            if (alpha.length == 0)
                throw new IllegalStateException("Tried to initialize tRNSIndexed chunk with zero length array.");
        }
    }

}
