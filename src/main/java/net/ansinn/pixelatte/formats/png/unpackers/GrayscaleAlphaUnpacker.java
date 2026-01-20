package net.ansinn.pixelatte.formats.png.unpackers;

import net.ansinn.pixelatte.output.DecodedImage;
import net.ansinn.pixelatte.output.DecodedImage16;
import net.ansinn.pixelatte.output.DecodedImage8;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.util.stream.IntStream;

public class GrayscaleAlphaUnpacker {
    public static DecodedImage unpackGrayscaleAlpha(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        return switch (header.bitDepth()) {
            case 8 -> unpackGrayscaleAlpha8Bit(filtered, header, chunkMap);
            case 16 -> unpackGrayscaleAlpha16Bit(filtered, header, chunkMap);
            default ->
                    throw new IllegalStateException("Unexpected bit-depth: " + header.bitDepth() + ", for color-type indexed.");
        };
    }

    private static DecodedImage unpackGrayscaleAlpha8Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var bpp = 2;
        byte[] pixels = new byte[width * height * 4];

        IntStream.range(0, height).parallel().forEach(y -> {
            var rowIn = y * width * bpp;
            var rowOut = y * width * 4;

            for (var x = 0; x < width; x++) {
                var inOffset = rowIn + x * 2;
                var outOffset = rowOut + x * 4;

                var gray = Byte.toUnsignedInt(filtered[inOffset]);
                var alpha = Byte.toUnsignedInt(filtered[inOffset + 1]);

                pixels[outOffset] = (byte) gray;
                pixels[outOffset + 1] = (byte) gray;
                pixels[outOffset + 2] = (byte) gray;
                pixels[outOffset + 3] = (byte) alpha;
            }
        });

        return new DecodedImage8(width, height, pixels, DecodedImage.Format.GRAY8, chunkMap);
    }

    private static DecodedImage unpackGrayscaleAlpha16Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var bpp = 4; // 2 bytes for grat + 2 bytes for alpha

        var pixels = new short[width * height * 4];

        IntStream.range(0,height).parallel().forEach(y -> {
            var rowIn = y * width * bpp;
            var rowOut = y * width * 4;

            for (var x = 0; x < width; x++) {
                var inOffset = rowIn + x * 4;
                var outOffset = rowOut + x * 4;

                var gray = ((filtered[inOffset] & 0xFF) << 8) | (filtered[inOffset + 1] & 0xFF);
                var alpha = ((filtered[inOffset + 2] & 0xFF) << 8) | (filtered[inOffset + 3] & 0xFF);

                pixels[outOffset] = (short) gray;
                pixels[outOffset + 1] = (short) gray;
                pixels[outOffset + 2] = (short) gray;
                pixels[outOffset + 3] = (short) alpha;
            }
        });

        return new DecodedImage16(width, height, pixels, DecodedImage.Format.GRAY16, chunkMap);
    }
}
