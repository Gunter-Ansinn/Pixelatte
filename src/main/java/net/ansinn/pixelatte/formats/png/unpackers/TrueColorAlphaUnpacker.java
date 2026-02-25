package net.ansinn.pixelatte.formats.png.unpackers;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorSpecies;
import net.ansinn.pixelatte.output.safe.StaticImage;
import net.ansinn.pixelatte.output.safe.StaticImage16;
import net.ansinn.pixelatte.output.safe.StaticImage8;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.util.stream.IntStream;

public class TrueColorAlphaUnpacker {

    private static final VectorSpecies<Byte> BYTE_SPECIES = ByteVector.SPECIES_PREFERRED;
    private static final VectorSpecies<Short> SHORT_SPECIES = ShortVector.SPECIES_PREFERRED;

    public static StaticImage unpackTrueColorAlpha(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        return switch (header.bitDepth()) {
            case 8 -> unpackTrueColorAlpha8Bit(filtered, header, chunkMap);
            case 16 -> unpackTrueColorAlpha16Bit(filtered, header, chunkMap);
            default ->
                    throw new IllegalStateException("Unexpected bit-depth: " + header.bitDepth() + ", for color-type truecolor alpha.");
        };
    }

    private static StaticImage unpackTrueColorAlpha8Bit(final byte[] filtered, final IHDR header, final ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var bpp = 4;
        var pixels = new byte[width * height * 4];

        System.arraycopy(filtered, 0, pixels, 0, width * height * 4);

        return new StaticImage8(width, height, pixels, StaticImage.Format.RGBA8, chunkMap);
    }

    private static StaticImage unpackTrueColorAlpha16Bit(byte[] filtered, IHDR header, ChunkMap chunkMap) {
        var width = header.width();
        var height = header.height();
        var bpp = 8;

        var pixels = new short[width * height * 4];

        // Pre calculate shuffle pattern to swap bytes from big endian to little endian
        var byteSwapShuffle = VectorShuffle.fromOp(BYTE_SPECIES, i -> i ^ 1);

        IntStream.range(0, height).parallel().forEach(y -> {
            var rowIn = y * width * bpp;
            var rowOut = y * width * 4;

            var totalShorts = width * 4;
            var loopLimit = totalShorts - SHORT_SPECIES.length();

            var byteIndex = 0;

            // Vector logic
            for (; byteIndex < loopLimit; byteIndex += SHORT_SPECIES.length()) {
                // Load our raw bytes
                var byteVec = ByteVector.fromArray(BYTE_SPECIES, filtered, rowIn + (byteIndex * 2));

                byteVec = byteVec.rearrange(byteSwapShuffle);

                var shortVec = byteVec.reinterpretAsShorts();
                shortVec.intoArray(pixels, rowOut + byteIndex);
            }

            // Scalar cleanup
            for (; byteIndex < totalShorts; byteIndex += 4) {
                int inOffset = rowIn + (byteIndex * 2);
                int outOffset = rowOut + byteIndex;

                short r = (short) (((filtered[inOffset] & 0xFF) << 8) | (filtered[inOffset + 1] & 0xFF));
                short g = (short) (((filtered[inOffset + 2] & 0xFF) << 8) | (filtered[inOffset + 3] & 0xFF));
                short b = (short) (((filtered[inOffset + 4] & 0xFF) << 8) | (filtered[inOffset + 5] & 0xFF));
                short a = (short) (((filtered[inOffset + 6] & 0xFF) << 8) | (filtered[inOffset + 7] & 0xFF));

                pixels[outOffset] = r;
                pixels[outOffset + 1] = g;
                pixels[outOffset + 2] = b;
                pixels[outOffset + 3] = a;
            }
        });

        return new StaticImage16(width, height, pixels, StaticImage.Format.RGBA16, chunkMap);
    }
}
