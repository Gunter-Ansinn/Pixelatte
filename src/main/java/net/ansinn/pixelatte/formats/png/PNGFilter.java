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
        var height = header.height();

        // Singular allocation for all operations
        var result = new byte[height * rowLength];

        var scanlineSize = rowLength + 1; // +1 for filter byte

        for (int y = 0; y < height; y++) {
            int rawOffset = y * scanlineSize; // Data compression start
            int resultOffset = y * rowLength; // Pixel write location
            int filterType = data[rawOffset] & 0xFF;

            int rawDataStart = rawOffset + 1;      // Skip the filter byte
            var bpp = header.getFilteringBpp();

            switch (filterType) {
                case 0 -> System.arraycopy(data, rawDataStart, result, resultOffset, rowLength);
                case 1 -> filterSub(data, rawDataStart, result, resultOffset, rowLength, bpp);
                case 2 -> filterUp(data, rawDataStart, result, resultOffset, rowLength);
                case 3 -> filterAverage(data, rawDataStart, result, resultOffset, rowLength, bpp);
                case 4 -> filterPaethScalar(data, rawDataStart, result, resultOffset, rowLength, bpp);
                default -> throw new IllegalStateException("Invalid filter type: " + filterType);
            }
        }

        return result;
    }

    /**
     * Applies a sub filter operation to an incoming stream of png bytes and modifies the given result array.
     * @param raw array of unfiltered bytes
     * @param rawOffset offset when scrolling through bytes
     * @param result where filter bytes will be written to
     * @param resultOffset offset of where to begin writing to the result array
     * @param length length of row
     * @param bpp bytes per pixel
     */
    public static void filterSub(byte[] raw, int rawOffset, byte[] result, int resultOffset, int length, int bpp) {
        // Start from the first pixel with no left neighbor
        if (bpp >= 0) System.arraycopy(raw, rawOffset, result, resultOffset, bpp);

        for (int byteIndex = bpp; byteIndex < length; byteIndex++) {
            int left = result[resultOffset + byteIndex - bpp] & 0xFF;
            int current = raw[rawOffset + byteIndex] & 0xFF;

            result[resultOffset + byteIndex] = (byte) (current + left);
        }
    }

    public static void filterUp(byte[] raw, int rawOffset, byte[] result, int resultOffset, int length) {
        // No previous row so we just copy the result
        if (resultOffset == 0) {
            System.arraycopy(raw, rawOffset, result, resultOffset, length);
            return;
        }

        // Define our previous location
        int previousOffset = resultOffset - length;

        final var stride = BYTE_SPECIES.length();
        final var loopBound = length - stride;
        var byteIndex = 0;

        for (; byteIndex <= loopBound; byteIndex += stride) {
            // Load raw bytes in
            var currentVec = ByteVector.fromArray(BYTE_SPECIES, raw, rawOffset + byteIndex);

            // Load in previous row
            var previousVec = ByteVector.fromArray(BYTE_SPECIES, result, previousOffset + byteIndex);

            // Add up vectors and store result
            var resultVec = currentVec.add(previousVec);
            resultVec.intoArray(result, resultOffset + byteIndex);
        }

        // Clean up trailing bytes scalarly
        for (; byteIndex < length; byteIndex++) {
            int above = result[previousOffset + byteIndex] & 0xFF;
            int current = raw[rawOffset + byteIndex] & 0xFF;

            result[resultOffset + byteIndex] = (byte) (current + above);
        }
    }

    public static void filterAverage(byte[] raw, int rawOffset, byte[] result, int resultOffset, int length, int bpp) {

        final int previousOffset = resultOffset - length;
        final boolean firstRow = (resultOffset == 0); // Above is just zero bytes if on first row

        // First BPP
        for (int byteIndex = 0; byteIndex < bpp; byteIndex++) {
            var above = firstRow ? 0 : (result[previousOffset + byteIndex] & 0xFF);
            var current = raw[rawOffset + byteIndex] & 0xFF;

            result[resultOffset + byteIndex] = (byte) (current + (above >>> 1));
        }

        // Rest of row
        for (var byteIndex = bpp; byteIndex < length; byteIndex++) {
            var left = result[resultOffset + byteIndex - bpp] & 0xFF;
            var above = firstRow ? 0 : (result[previousOffset + byteIndex] & 0xFF);
            var current = raw[rawOffset + byteIndex] & 0xFF;

            var average = (left + above) >>> 1;
            result[resultOffset + byteIndex] = (byte) (current + average);
        }
    }

    public static void filterPaethScalar(byte[] raw, int rawOffset, byte[] result, int resultOffset, int length, int bpp) {
        final var previousOffset = resultOffset - length;
        final boolean firstRow = (resultOffset == 0); // Above is just zero bytes if on first row

        // First bpp
        for (int byteIndex = 0; byteIndex < bpp; byteIndex++) {
            int above = firstRow ? 0 : (result[previousOffset + byteIndex] & 0xFF);
            int current = raw[rawOffset + byteIndex] & 0xFF;

            result[resultOffset + byteIndex] = (byte) (current + above);
        }

        // Rest of row
        for (int byteIndex = bpp; byteIndex < length; byteIndex++) {
            int left = result[resultOffset + byteIndex - bpp] & 0xFF;
            int above = firstRow ? 0 : (result[previousOffset + byteIndex] & 0xFF);
            int upperLeft = firstRow ? 0 : (result[previousOffset + byteIndex - bpp] & 0xFF);

            int current = raw[rawOffset + byteIndex] & 0xFF;

            result[resultOffset + byteIndex] = (byte) (current + paethPredictor(left, above, upperLeft));
        }
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

}
