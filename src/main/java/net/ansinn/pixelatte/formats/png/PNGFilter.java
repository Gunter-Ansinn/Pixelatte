package net.ansinn.pixelatte.formats.png;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.util.Arrays;

public class PNGFilter {

    private static final VectorSpecies<Byte> BYTE_SPECIES = ByteVector.SPECIES_PREFERRED;


    public static byte[] process(byte[] data, IHDR header) {
        var bitsPerPixel = header.bitDepth() * header.colorType().getChannels();
        var rowBits = header.width() * bitsPerPixel;
        var rowLength = (rowBits + 7) / 8;

        var bpp = header.getBytesPerPixel();

        var scanlineSize = rowLength + 1;
        var previous = new byte[rowLength];
        var result = new byte[header.height() * rowLength];

        for (int y = 0; y < header.height(); y++) {
            var offset = y * scanlineSize;
            var filterType = data[offset] & 0xFF;
            var raw = Arrays.copyOfRange(data, offset + 1, offset + scanlineSize);

            var recon = switch (filterType) {
                case 0 -> raw;
                case 1 -> filterSub(raw, bpp);
                case 2 -> filterUp(raw, previous);
                case 3 -> filterAverageSIMD(raw, previous, bpp);
                case 4 -> filterPaethScalar(raw, previous, bpp);
                default -> throw new IllegalStateException("Invalid filter type: " + filterType);
            };

            System.arraycopy(recon, 0, result, y * rowLength, rowLength);
            System.arraycopy(recon, 0, previous, 0, rowLength);
        }

        return result;
    }

    public static byte[] filterSub(byte[] raw, int bpp) {
        final var length = raw.length;
        var result = new byte[length];

        System.arraycopy(raw, 0, result , 0, bpp);

        final var stride = BYTE_SPECIES.length();
        var byteIndex = bpp;

        for (; byteIndex <= length - stride; byteIndex += stride) {
            var currVec = ByteVector.fromArray(BYTE_SPECIES, raw, byteIndex);
            var leftVec = ByteVector.fromArray(BYTE_SPECIES, result, byteIndex - bpp);
            var resultVec = currVec.add(leftVec);

            resultVec.intoArray(result, byteIndex);
        }

        for (; byteIndex < length; byteIndex++) {
            int left = result[byteIndex - bpp] & 0xFF;
            int curr = raw[byteIndex] & 0xFF;
            result[byteIndex] = (byte) (curr + left);
        }

        return result;
    }

    public static byte[] filterUp(byte[] raw, byte[] previous) {
        final var length = raw.length;
        var result = new byte[length];

        final var stride = BYTE_SPECIES.length();
        var byteIndex = 0;

        for (; byteIndex <= length - stride; byteIndex += stride) {
            var currVec = ByteVector.fromArray(BYTE_SPECIES, raw, byteIndex);
            var prevVec = ByteVector.fromArray(BYTE_SPECIES, previous, byteIndex);
            var resultVec = currVec.add(prevVec);

            resultVec.intoArray(result, byteIndex);
        }

        for (; byteIndex < length; byteIndex++) {
            int above = previous[byteIndex] & 0xFF;
            int curr = raw[byteIndex] & 0xFF;

            result[byteIndex] = (byte) (curr + above);
        }

        return result;
    }

    public static byte[] filterAverageSIMD(byte[] raw, byte[] previous, int bpp) {
        final var length = raw.length;
        var result = new byte[length];

        for (int i = 0; i < bpp; i++) {
            var above = previous[i] & 0xFF;
            var curr = raw[i] & 0xFF;
            result[i] = (byte) (curr + (above >>> 1));
        }

        final var stride = BYTE_SPECIES.length();
        var byteIndex = bpp;

        for (; byteIndex <= length - stride; byteIndex += stride) {
            var currVec = ByteVector.fromArray(BYTE_SPECIES, raw, byteIndex);
            var leftVec = ByteVector.fromArray(BYTE_SPECIES, result, byteIndex - bpp);
            var aboveVec = ByteVector.fromArray(BYTE_SPECIES, previous, byteIndex);

            var avgVec = leftVec.add(aboveVec).lanewise(VectorOperators.ASHR, 1);
            var resultVec = currVec.add(avgVec);

            resultVec.intoArray(result, byteIndex);
        }

        for (; byteIndex < length; byteIndex++) {
            var left = result[byteIndex - bpp] & 0xFF;
            var above = previous[byteIndex] & 0xFF;
            var curr = raw[byteIndex] & 0xFF;

            var avg = (left + above) >>> 1;
            result[byteIndex] = (byte) (curr + avg);
        }

        return result;
    }

    public static byte[] filterAverageScalar(byte[] raw, byte[] previous, int bpp) {
        final var length = raw.length;
        var result = new byte[length];

        for (int i = 0; i < bpp; i++) {
            var above = previous[i] & 0xFF;
            var curr = raw[i] & 0xFF;
            result[i] = (byte) (curr + (above >>> 1));
        }

        for (var byteIndex = bpp; byteIndex < length; byteIndex++) {
            var left = result[byteIndex - bpp] & 0xFF;
            var above = previous[byteIndex] & 0xFF;
            var curr = raw[byteIndex] & 0xFF;

            var avg = (left + above) >>> 1;
            result[byteIndex] = (byte) (curr + avg);
        }

        return result;
    }

    public static byte[] filterPaethScalar(byte[] raw, byte[] previous, int bpp) {
        final int length = raw.length;
        byte[] result = new byte[length];

        for (int i = 0; i < bpp; i++) {
            int curr = raw[i] & 0xFF;
            int above = previous[i] & 0xFF;
            result[i] = (byte) (curr + paethPredictor(0, above, 0));
        }

        for (int i = bpp; i < length; i++) {
            int left = result[i - bpp] & 0xFF;
            int above = previous[i] & 0xFF;
            int upperLeft = previous[i - bpp] & 0xFF;
            int curr = raw[i] & 0xFF;

            result[i] = (byte) (curr + paethPredictor(left, above, upperLeft));
        }

        return result;
    }

    private static int paethPredictor(int a, int b, int c) {
        int p = a + b - c;
        int pa = Math.abs(p - a);
        int pb = Math.abs(p - b);
        int pc = Math.abs(p - c);

        if (pa <= pb && pa <= pc) return a;
        if (pb <= pc) return b;
        return c;
    }

}
