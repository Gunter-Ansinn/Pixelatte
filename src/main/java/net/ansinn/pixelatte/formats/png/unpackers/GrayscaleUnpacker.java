package net.ansinn.pixelatte.formats.png.unpackers;

import net.ansinn.pixelatte.DecodedImage;
import net.ansinn.pixelatte.DecodedImage8;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;
import net.ansinn.pixelatte.formats.png.layout.chunks.tRNS;

import java.util.stream.IntStream;

public class GrayscaleUnpacker {
    public static DecodedImage unpackGrayscale(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        return switch (header.bitDepth()) {
            case 1 -> unpackGrayscale1Bit(filtered, header, chunkMap);
            case 2 -> unpackGrayscale2Bit(filtered, header, chunkMap);
            case 4 -> unpackGrayscale4Bit(filtered, header, chunkMap);
            case 8 -> unpackGrayscale8Bit(filtered, header, chunkMap);
            case 16 -> unpackGrayscale16Bit(filtered, header, chunkMap);
            default ->
                    throw new IllegalStateException("Unexpected bit-depth: " + header.bitDepth() + ", for color-type grayscale.");
        };
    }

    private static DecodedImage unpackGrayscale1Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var pixels = new byte[width * height * 4];
        var transparency = chunkMap.getFirst(tRNS.Grayscale.class);

        IntStream.range(0, height).parallel().forEach(y -> {
            var rowIn = y * ((width + 7) / 8);
            var rowOut = y * width * 4;

            int x = 0;
            for (int byteIndex = 0; byteIndex < ((width + 7) / 8); byteIndex++) {
                int b = filtered[rowIn + byteIndex] & 0xFF;
                for (int bit = 7; bit >= 0 && x < width; bit--) {
                    int value = (b >> bit) & 1;
                    int gray = value == 1 ? 255 : 0;
                    int alpha = 0xFF;
                    if (transparency.isPresent() && gray == transparency.get().grayValue()) {
                        alpha = 0;
                    }
                    int out = rowOut + x * 4;
                    pixels[out] = (byte) gray;
                    pixels[out + 1] = (byte) gray;
                    pixels[out + 2] = (byte) gray;
                    pixels[out + 3] = (byte) alpha;
                    x++;
                }
            }
        });

        return new DecodedImage8(width, height, pixels, DecodedImage.Format.GRAY8, chunkMap);
    }

    private static DecodedImage unpackGrayscale2Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var pixels = new byte[width * height * 4];
        var transparency = chunkMap.getFirst(tRNS.Grayscale.class);

        IntStream.range(0, height).parallel().forEach(y -> {
            var rowIn = y * ((width + 3) / 4);
            var rowOut = y * width * 4;

            int x = 0;
            for (int byteIndex = 0; byteIndex < ((width + 3) / 4); byteIndex++) {
                int b = filtered[rowIn + byteIndex] & 0xFF;
                for (int shift = 6; shift >= 0 && x < width; shift -= 2) {
                    int value = (b >> shift) & 0b11;
                    int gray = value * 85;
                    int alpha = 0xFF;
                    if (transparency.isPresent() && gray == transparency.get().grayValue()) {
                        alpha = 0;
                    }
                    int out = rowOut + x * 4;
                    pixels[out] = (byte) gray;
                    pixels[out + 1] = (byte) gray;
                    pixels[out + 2] = (byte) gray;
                    pixels[out + 3] = (byte) alpha;
                    x++;
                }
            }
        });

        return new DecodedImage8(width, height, pixels, DecodedImage.Format.GRAY8, chunkMap);
    }

    private static DecodedImage unpackGrayscale4Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        int width = header.width();
        int height = header.height();
        byte[] pixels = new byte[width * height * 4];
        var transparency = chunkMap.getFirst(tRNS.Grayscale.class);

        IntStream.range(0, height).parallel().forEach(y -> {
            int rowIn = y * ((width + 1) / 2); // each byte = 2 pixels
            int rowOut = y * width * 4;
            int x = 0;

            for (int byteIndex = 0; byteIndex < (width + 1) / 2; byteIndex++) {
                int b = filtered[rowIn + byteIndex] & 0xFF;

                for (int shift = 4; shift >= 0 && x < width; shift -= 4) {
                    int value = (b >> shift) & 0x0F;
                    int gray = value * 17;
                    int alpha = 0xFF;

                    if (transparency.isPresent() && transparency.get().grayValue() == gray)
                        alpha = 0;

                    int out = rowOut + x * 4;
                    pixels[out] = (byte) gray;
                    pixels[out + 1] = (byte) gray;
                    pixels[out + 2] = (byte) gray;
                    pixels[out + 3] = (byte) alpha;
                    x++;
                }
            }
        });

        return new DecodedImage8(width, height, pixels, DecodedImage.Format.GRAY8, chunkMap);
    }

    private static DecodedImage unpackGrayscale8Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var pixels = new byte[width * height * 4];
        var transparency = chunkMap.getFirst(tRNS.Grayscale.class);

        IntStream.range(0, height).parallel().forEach(y -> {
            var rowIn = y * width;
            var rowOut = y * width * 4;

            for (var x = 0; x < width; x++) {
                var gray = Byte.toUnsignedInt(filtered[rowIn + x]);
                var alpha = 0xFF;

                if (transparency.isPresent() && gray == transparency.get().grayValue())
                    alpha = 0x00;

                var index = rowOut + x * 4;
                pixels[index] = (byte) gray;
                pixels[index + 1] = (byte) gray;
                pixels[index + 2] = (byte) gray;
                pixels[index + 3] = (byte) alpha;
            }

        });

        return new DecodedImage8(width, height, pixels, DecodedImage.Format.GRAY8, chunkMap);
    }

    private static DecodedImage unpackGrayscale16Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        int width = header.width();
        int height = header.height();
        byte[] pixels = new byte[width * height * 4];
        var transparency = chunkMap.getFirst(tRNS.Grayscale.class);

        IntStream.range(0, height).parallel().forEach(y -> {
            int rowIn = y * width * 2; // 2 bytes per pixel
            int rowOut = y * width * 4;

            for (int x = 0; x < width; x++) {
                int inOffset = rowIn + x * 2;
                int gray16 = ((filtered[inOffset] & 0xFF) << 8) | (filtered[inOffset + 1] & 0xFF);
                int gray = gray16 >> 8; // take high byte only
                int alpha = 0xFF;

                if (transparency.isPresent() && gray16 == transparency.get().grayValue())
                    alpha = 0;

                int out = rowOut + x * 4;
                pixels[out] = (byte) gray;
                pixels[out + 1] = (byte) gray;
                pixels[out + 2] = (byte) gray;
                pixels[out + 3] = (byte) alpha;
            }
        });

        return new DecodedImage8(width, height, pixels, DecodedImage.Format.GRAY8, chunkMap);
    }
}
