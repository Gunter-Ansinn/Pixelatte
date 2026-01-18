package net.ansinn.pixelatte.formats.png;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorSpecies;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.util.Arrays;

public class PNGFilter {

    private static final VectorSpecies<Byte> BYTE_SPECIES = ByteVector.SPECIES_PREFERRED;


    public static byte[] process(byte[] data, IHDR header) {
        var rowBits = header.width() * header.getBitsPerPixel();
        var rowLength = (rowBits + 7) / 8;

        var previous = new byte[rowLength];
        var result = new byte[header.height() * rowLength];

        for (int y = 0; y < header.height(); y++) {
            var reconstructed = applyRowFilter(data, y, rowLength, previous, header);

            System.arraycopy(reconstructed, 0, result, y * rowLength, rowLength);
            System.arraycopy(reconstructed, 0, previous, 0, rowLength);
        }

        return result;
    }

    public static byte[] applyRowFilter(byte[] data, int rowIndex, int rowLength, byte[] previous, IHDR header) {
        // Scanline calculations
        var scanlineSize = rowLength + 1;
        var offset = rowIndex * scanlineSize;

        // Filter types:
        // 0 - None
        // 1 - Sub
        // 2 - Up
        // 3 - Average
        // 4 - Paeth
        var filterType = data[offset] & 0xFF;

        if (filterType > 4)
            throw new IllegalStateException("Illegal filter type: " + filterType + ", on PNG filter row: " + rowIndex);

        if (offset + scanlineSize > data.length)
            throw new IllegalStateException("Input data too short for row " + rowIndex + ": expected " + scanlineSize + " bytes.");

        var raw = Arrays.copyOfRange(data, offset + 1, offset + scanlineSize);
        var bpp = header.getFilteringBpp();

        // Apply filter to current row
        return switch (filterType) {
            case 0 -> raw;
            case 1 -> filterSub(raw, bpp);
            case 2 -> filterUp(raw, previous);
            case 3 -> filterAverage(raw, previous, bpp);
            case 4 -> filterPaethScalar(raw, previous, bpp);
            default -> throw new IllegalStateException("Invalid filter type: " + filterType);
        };
    }

    public static byte[] filterSub(byte[] raw, int bpp) {
        final var length = raw.length;
        var result = new byte[length];

        // Start from the first pixel with no left neighbor
        for (int i = 0; i < bpp; i++) {
            result[i] = raw[i];
        }

        for (int i = bpp; i < length; i++) {
            int left = result[i - bpp] & 0xFF;
            int curr = raw[i] & 0xFF;
            result[i] = (byte) (curr + left);
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

    public static byte[] filterAverage(byte[] raw, byte[] previous, int bpp) {
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

    public static int paethPredictor(int a, int b, int c) {
        int p = a + b - c;
        int pa = Math.abs(p - a);
        int pb = Math.abs(p - b);
        int pc = Math.abs(p - c);

        if (pa <= pb && pa <= pc) return a;
        if (pb <= pc) return b;
        return c;
    }

    // Debug helper to dump row bytes as readable pixel groups
    private static void dumpRowBytes(byte[] row, int bpp) {
        for (int i = 0; i < row.length; i += bpp) {
            System.out.print("[");
            for (int j = 0; j < bpp; j++) {
                if (i + j < row.length)
                    System.out.printf("%3d", row[i + j] & 0xFF);
                if (j < bpp - 1)
                    System.out.print(", ");
            }
            System.out.print("] ");
        }
        System.out.println();
    }

}
