package net.ansinn.pixelatte.formats.png.unpackers;

import net.ansinn.pixelatte.output.DecodedImage;
import net.ansinn.pixelatte.output.DecodedImage16;
import net.ansinn.pixelatte.output.DecodedImage8;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;
import net.ansinn.pixelatte.formats.png.layout.chunks.tRNS;

import java.util.stream.IntStream;

public class TrueColorUnpacker {

    public static DecodedImage unpackTrueColor(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        return switch (header.bitDepth()) {
            case 8 -> unpackTrueColor8Bit(filtered, header, chunkMap);
            case 16 -> unpackTrueColor16Bit(filtered, header, chunkMap);
            default ->
                    throw new IllegalStateException("Unexpected bit-depth: " + header.bitDepth() + ", for color-type truecolor.");
        };
    }

    public static DecodedImage unpackTrueColor8Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var bpp = 3;
        var pixels = new byte[width * height * 4];
        var transparency = chunkMap.getFirst(tRNS.TrueColor.class);

        IntStream.range(0, height).parallel().forEach(y -> {
            var rowIn = y * width * bpp;
            var rowOut = y * width * 4;

            for (int x = 0; x < width; x++) {
                var inOffset = rowIn + x * 3;
                var outOffset = rowOut + x * 4;

                var red = Byte.toUnsignedInt(filtered[inOffset]);
                var green = Byte.toUnsignedInt(filtered[inOffset + 1]);
                var blue = Byte.toUnsignedInt(filtered[inOffset + 2]);

                var alpha = 0xFF;
                if (transparency.isPresent()) {
                    var t = transparency.get();

                    if (t.red() == red && t.green() == green && t.blue() == blue)
                        alpha = 0x00;
                }

                pixels[outOffset] = (byte) red;
                pixels[outOffset + 1] = (byte) green;
                pixels[outOffset + 2] = (byte) blue;
                pixels[outOffset + 3] = (byte) alpha;
            }
        });

        return new DecodedImage8(width, height, pixels, DecodedImage.Format.RGBA8, chunkMap);
    }

    private static DecodedImage unpackTrueColor16Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var bpp = 6;
        var pixels = new short[width * height * 4];
        var transparency = chunkMap.getFirst(tRNS.TrueColor.class);

        IntStream.range(0, height).parallel().forEach(y -> {
            var rowIn = y * width * bpp;
            var rowOut = y * width * 4;

            for (var x = 0; x < width; x++) {
                var inOffset = rowIn + x * 6;
                var outOffset = rowOut + x * 4;

                // Collect RGB in 16-bit format for sake of clarity
                var red16 = ((filtered[inOffset] & 0xFF) << 8) | (filtered[inOffset + 1] & 0xFF);
                var green16 = ((filtered[inOffset + 2] & 0xFF) << 8) | (filtered[inOffset + 3] & 0xFF);
                var blue16 = ((filtered[inOffset + 4] & 0xFF) << 8) | (filtered[inOffset + 5] & 0xFF);

                var alpha = 0xFF;
                // Compare against 16 bit values for transparency
                if (transparency.isPresent()) {
                    var t = transparency.get();
                    if (t.red() == red16 && t.green() == green16 && t.blue() == blue16) {
                        alpha = 0x00;
                    }
                }

                pixels[outOffset]     = (short) red16;
                pixels[outOffset + 1] = (short) green16;
                pixels[outOffset + 2] = (short) blue16;
                pixels[outOffset + 3] = (short) alpha;
            }
        });

        return new DecodedImage16(width, height, pixels, DecodedImage.Format.RGBA16, chunkMap);
    }

}
