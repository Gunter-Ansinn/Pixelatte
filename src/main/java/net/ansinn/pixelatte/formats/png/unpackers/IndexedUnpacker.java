package net.ansinn.pixelatte.formats.png.unpackers;

import net.ansinn.pixelatte.output.DecodedImage;
import net.ansinn.pixelatte.output.DecodedImage8;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;
import net.ansinn.pixelatte.formats.png.layout.chunks.PLTE;
import net.ansinn.pixelatte.formats.png.layout.chunks.tRNS;

import java.util.stream.IntStream;

public class IndexedUnpacker {

    private static final byte[][] LUT_1BIT = new byte[256][8];
    private static final byte[][] LUT_2BIT = new byte[256][4];
    private static final byte[][] LUT_4BIT = new byte[256][2];

    static {
        for (var byteIndex = 0; byteIndex < 256; byteIndex++) {
            // 1-bit: 8 pixels per byte
            for (int bit = 0; bit < 8; bit++)
                LUT_1BIT[byteIndex][bit] = (byte) ((byteIndex >> (7 - bit)) & 0x01);

            // 2-bit: 4 pixels per byte
            for (int bit = 0; bit < 4; bit++)
                LUT_2BIT[byteIndex][bit] = (byte) ((byteIndex >> (6 - bit * 2)) & 0x03);

            // 4-bit: 2 pixels per byte
            for (int bit = 0; bit < 2; bit++)
                LUT_4BIT[byteIndex][bit] = (byte) ((byteIndex >> (4 - bit * 4)) & 0x0F);
        }
    }

    public static DecodedImage unpackIndexed(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        return switch (header.bitDepth()) {
            case 1 -> unpackIndexed1Bit(filtered, header, chunkMap);
            case 2 -> unpackIndexed2Bit(filtered, header, chunkMap);
            case 4 -> unpackIndexed4Bit(filtered, header, chunkMap);
            case 8 -> unpackIndexed8Bit(filtered, header, chunkMap);
            default ->
                    throw new IllegalStateException("Unexpected bit-depth: " + header.bitDepth() + ", for color-type indexed.");
        };
    }

    private static DecodedImage unpackIndexed1Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var palette = chunkMap.getFirst(PLTE.class)
                .orElseThrow(() -> new IllegalStateException("PLTE chunk required for indexed PNG type."));

        var transparency = chunkMap.getFirst(tRNS.Indexed.class);
        byte[] alphaPalette = transparency.map(tRNS.Indexed::alpha).orElse(null);

        var colors = palette.colors();
        var pixels = new byte[width * height * 4];

        IntStream.range(0, header.height()).parallel().forEach(y -> {
            var rowIn = y * ((width + 7) / 8);
            var rowOut = y * width * 4;
            var x = 0;

            for (var i = 0; i < (width + 7) / 8; i++) {

                // Get the LUT entry for this whole byte
                byte[] indices = LUT_1BIT[filtered[rowIn + i] & 0xFF];

                for (int pixelIndex = 0; pixelIndex < 8 && x < width; pixelIndex++) {
                    int index = indices[pixelIndex];
                    var color = colors[index];
                    var out = rowOut + x * 4;

                    pixels[out] = color.red();
                    pixels[out + 1] = color.green();
                    pixels[out + 2] = color.blue();

                    // Alpha lookup
                    pixels[out + 3] = (alphaPalette != null) ? alphaPalette[index] : (byte) 0xFF;

                    x++;
                }
            }
        });

        return new DecodedImage8(width, height, pixels, DecodedImage.Format.RGBA8, chunkMap);
    }

    private static DecodedImage unpackIndexed2Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var palette = chunkMap.getFirst(PLTE.class)
                .orElseThrow(() -> new IllegalStateException("PLTE chunk required for indexed PNG type."));

        var transparency = chunkMap.getFirst(tRNS.Indexed.class);
        byte[] alphaPalette = transparency.map(tRNS.Indexed::alpha).orElse(null);

        var colors = palette.colors();
        var pixels = new byte[width * height * 4];

        IntStream.range(0, header.height()).parallel().forEach(y -> {
            var rowIn = y * ((width + 3) / 4);
            var rowOut = y * width * 4;
            var x = 0;

            for (var i = 0; i < (width + 3) / 4; i++) {
                byte[] indices = LUT_2BIT[filtered[rowIn + i] & 0xFF];

                // 4 Pixels per byte
                for (int pixelIndex = 0; pixelIndex < 4 && x < width; pixelIndex++) {
                    int index = indices[pixelIndex];
                    var color = colors[index];
                    var out = rowOut + x * 4;

                    pixels[out] = color.red();
                    pixels[out + 1] = color.green();
                    pixels[out + 2] = color.blue();

                    // Check if alpha palette exists, otherwise opaque
                    pixels[out + 3] = (alphaPalette != null) ? alphaPalette[index] : (byte) 0xFF;

                    x++;
                }
            }
        });

        return new DecodedImage8(width, height, pixels, DecodedImage.Format.RGBA8, chunkMap);
    }

    private static DecodedImage unpackIndexed4Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var palette = chunkMap.getFirst(PLTE.class)
                .orElseThrow(() -> new IllegalStateException("PLTE chunk required for indexed PNG type."));

        var transparency = chunkMap.getFirst(tRNS.Indexed.class);
        byte[] alphaPalette = transparency.map(tRNS.Indexed::alpha).orElse(null);

        var colors = palette.colors();
        var pixels = new byte[width * height * 4];

        IntStream.range(0, header.height()).parallel().forEach(y -> {
            var rowIn = y * ((width + 1) / 2);
            var rowOut = y * width * 4;
            var x = 0;

            for (var i = 0; i < (width + 1) / 2; i++) {
                byte[] indices = LUT_4BIT[filtered[rowIn + i] & 0xFF];

                // 2 Pixels per byte
                for (int pixelIndex = 0; pixelIndex < 2 && x < width; pixelIndex++) {
                    int index = indices[pixelIndex];
                    var color = colors[index];
                    var out = rowOut + x * 4;

                    pixels[out] = color.red();
                    pixels[out + 1] = color.green();
                    pixels[out + 2] = color.blue();
                    pixels[out + 3] = (alphaPalette != null) ? alphaPalette[index] : (byte) 0xFF;

                    x++;
                }
            }
        });

        return new DecodedImage8(width, height, pixels, DecodedImage.Format.RGBA8, chunkMap);
    }

    private static DecodedImage unpackIndexed8Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var palette = chunkMap.getFirst(PLTE.class)
                .orElseThrow(() -> new IllegalStateException("PLTE chunk required for indexed PNG type."));
        var transparency = chunkMap.getFirst(tRNS.Indexed.class);

        var colors = palette.colors();
        var pixels = new byte[width * height * 4];

        IntStream.range(0, header.height()).parallel().forEach(y -> {
            var rowStart = y * width;
            var outStart = rowStart * 4;

            for (var x = 0; x < width; x++) {
                var index = Byte.toUnsignedInt(filtered[rowStart + x]);

                var i = outStart + x * 4;
                var color = colors[index];
                pixels[i] = color.red();
                pixels[i + 1] = color.green();
                pixels[i + 2] = color.blue();

                var alpha = (byte) 0xFF;

                if (transparency.isPresent())
                    alpha = transparency.get().alpha()[index];

                pixels[i + 3] = alpha;
            }
        });

        return new DecodedImage8(width, height, pixels, DecodedImage.Format.RGBA8, chunkMap);
    }
}
