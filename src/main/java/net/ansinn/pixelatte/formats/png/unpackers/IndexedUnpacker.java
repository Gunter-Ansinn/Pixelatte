package net.ansinn.pixelatte.formats.png.unpackers;

import net.ansinn.pixelatte.DecodedImage;
import net.ansinn.pixelatte.DecodedImage8;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;
import net.ansinn.pixelatte.formats.png.layout.chunks.PLTE;
import net.ansinn.pixelatte.formats.png.layout.chunks.tRNS;

import java.util.stream.IntStream;

public class IndexedUnpacker {

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

        var colors = palette.colors();
        var pixels = new byte[width * height * 4];

        IntStream.range(0, header.height()).parallel().forEach(y -> {
            var rowIn = y * ((width + 7) / 8);
            var rowOut = y * width * 4;
            var x = 0;

            for (var i = 0; i < (width + 7) / 8; i++) {
                var filteredVal = filtered[rowIn + i] & 0xFF;
                for (var bit = 7; bit >= 0 && x < width; bit--) {
                    var index = (filteredVal >> bit) & 0x01;
                    var color = colors[index];
                    var out = rowOut + x * 4;

                    pixels[out] = color.red();
                    pixels[out + 1] = color.green();
                    pixels[out + 2] = color.blue();
                    pixels[out + 3] = transparency.map(t -> t.alpha()[index]).orElse((byte) 0xFF);

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

        var colors = palette.colors();
        var pixels = new byte[width * height * 4];

        IntStream.range(0, header.height()).parallel().forEach(y -> {
            var rowIn = y * ((width + 3) / 4);
            var rowOut = y * width * 4;
            var x = 0;

            for (var i = 0; i < (width + 3) / 4; i++) {
                var filteredVal = filtered[rowIn + i] & 0xFF;
                for (var shift = 6; shift >= 0 && x < width; shift -= 2) {
                    var index = (filteredVal >> shift) & 0x03;
                    var color = colors[index];
                    var out = rowOut + x * 4;

                    pixels[out] = color.red();
                    pixels[out + 1] = color.green();
                    pixels[out + 2] = color.blue();
                    pixels[out + 3] = transparency.map(t -> t.alpha()[index]).orElse((byte) 0xFF);

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

        var colors = palette.colors();
        var pixels = new byte[width * height * 4];

        IntStream.range(0, header.height()).parallel().forEach(y -> {
            var rowIn = y * ((width + 1) / 2);
            var rowOut = y * width * 4;
            var x = 0;

            for (var i = 0; i < (width + 1) / 2; i++) {
                var filteredVal = filtered[rowIn + i] & 0xFF;

                for (var shift = 4; shift >= 0 && x < width; shift -= 4) {
                    var index = (filteredVal >> shift) & 0x0F;
                    var color = colors[index];
                    var out = rowOut + x * 4;

                    pixels[out] = color.red();
                    pixels[out + 1] = color.green();
                    pixels[out + 2] = color.blue();
                    pixels[out + 3] = transparency.map(t -> t.alpha()[index]).orElse((byte) 0xFF);

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
