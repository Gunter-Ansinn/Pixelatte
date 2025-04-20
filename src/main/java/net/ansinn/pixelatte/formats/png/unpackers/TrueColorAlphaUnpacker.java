package net.ansinn.pixelatte.formats.png.unpackers;

import net.ansinn.pixelatte.DecodedImage;
import net.ansinn.pixelatte.DecodedImage16;
import net.ansinn.pixelatte.DecodedImage8;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.util.stream.IntStream;

public class TrueColorAlphaUnpacker {
    public static DecodedImage unpackTrueColorAlpha(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        return switch (header.bitDepth()) {
            case 8 -> unpackTrueColorAlpha8Bit(filtered, header, chunkMap);
            case 16 -> unpackTrueColorAlpha16Bit(filtered, header, chunkMap);
            default ->
                    throw new IllegalStateException("Unexpected bit-depth: " + header.bitDepth() + ", for color-type truecolor alpha.");
        };
    }

    private static DecodedImage unpackTrueColorAlpha8Bit(final byte[] filtered, final IHDR header, final ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var bpp = 4;
        var pixels = new byte[width * height * 4];

        IntStream.range(0, height).parallel().forEach(y -> {
            var rowIn = y * width * bpp;
            var rowOut = y * width * 4;

            for (int x = 0; x < width; x++) {
                var inOffset = rowIn + x * 4;
                var outOffset = rowOut + x * 4;

                pixels[outOffset] = filtered[inOffset];
                pixels[outOffset + 1] = filtered[inOffset + 1];
                pixels[outOffset + 2] = filtered[inOffset + 2];
                pixels[outOffset + 3] = filtered[inOffset + 3];
            }
        });

        return new DecodedImage8(width, height, pixels, DecodedImage.Format.RGBA8, chunkMap);
    }

    private static DecodedImage unpackTrueColorAlpha16Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var bpp = 8;

        var pixels = new short[width * height * 4];

        IntStream.range(0, height).parallel().forEach(y -> {
            var rowIn = y * width * bpp;
            var rowOut = y * width * 4;

            for (int x = 0; x < width; x++) {
                var inOffset = rowIn + x * bpp;
                var outOffset = rowOut + x * 4;

                var red = ((filtered[inOffset] & 0xFF) << 8) | (filtered[inOffset + 1] & 0xFF);
                var green = ((filtered[inOffset + 2] & 0xFF) << 8) | (filtered[inOffset + 3] & 0xFF);
                var blue = ((filtered[inOffset + 4] & 0xFF) << 8) | (filtered[inOffset + 5] & 0xFF);
                var alpha = ((filtered[inOffset + 6] & 0xFF) << 8) | (filtered[inOffset + 7] & 0xFF);

                pixels[outOffset] = (short) red;
                pixels[outOffset + 1] = (short) green;
                pixels[outOffset + 2] = (short) blue;
                pixels[outOffset + 3] = (short) alpha;
            }
        });

        return new DecodedImage16(width, height, pixels, DecodedImage.Format.RGBA8, chunkMap);
    }
}
