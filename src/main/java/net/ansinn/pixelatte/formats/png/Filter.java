package net.ansinn.pixelatte.formats.png;


import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class Filter {

    /**
     *
     * @param header
     * @param input
     * @return
     */
    public static FilterResult parseFilter(IHDR header, NibbledOutput input) {
        return switch (input) {
            case NibbledOutput.Bits(var bits) -> filterBits(bits, header);
            case NibbledOutput.BitPairs(var pairs) -> filterPairs(pairs, header);
            case NibbledOutput.Nibbles(var nibbles) -> filterNibbles(nibbles, header);
            case NibbledOutput.Bytes(var bytes) -> filterBytes(bytes, header);
            case NibbledOutput.Shorts(var shorts) -> null;
            default -> {
                // Ideally this is *forever* unreachable however it cannot hurt to be safe if someone interferes with the JAR.
                System.err.println("The following Nibbled output is an invalid permutation: " + input.getClass().getTypeName());
                yield FilterResult.EMPTY;
            }
        };
    }

    /**
     * Applies constraints of bit sizes to filtering. That means no value will exceed 1 or go below 0;
     * @param bits the bytes representing bits.
     * @return filtered result to be handled by Texture loading.
     */
    private static FilterResult filterBits(byte[] bits, IHDR header) {
        var result = new byte[header.width() * header.height()];
        var prevLine = new byte[header.width()];

        // The array is composed of 1 byte and then width amount of bits.
        for (var rowIndex = 0; rowIndex < header.height(); rowIndex++) {
            var filter = rowIndex * (header.width() + 1);
            var row = new byte[header.width()];

            var step = (rowIndex * (header.width() + 1));

            System.arraycopy(bits, 1 + step, row, 0, header.width());

            System.out.println("Row data: " + Arrays.toString(row) + ", LEN: " + row.length + ", NUM: " + rowIndex);
        }
        System.out.println("result = " + Arrays.toString(result));
        return new FilterResult.ByteResult(result);
    }

    private static FilterResult filterPairs(byte[] pairs, IHDR header) {
        return null;
    }

    private static FilterResult filterNibbles(byte[] nibbles, IHDR header) {
        return null;
    }

    private static FilterResult filterBytes(int[] bytes, IHDR header) {
        return null;
    }

    private byte[] filterByteArray(byte filter, byte[] current, byte[] last) {
        return switch (filter) {
            case 0 -> current;
            case 1 -> {
                yield null;
            }
            default -> throw new IllegalStateException("Unexpected filter: " + filter);
        };
    }

    /**
     * Each byte is replaced by the difference between it and the byte before it in the scanline.
     * @param current current scanline being filtered
     * @param bpp the bytes per pixel
     * @return
     */
    private byte[] applyByteSubFilter(byte[] current, byte bpp) {
        byte[] result = new byte[current.length];

        for (var i = 0; i < current.length; i++)
            result[i] = (i < bpp) // Dear Java architects please make If and Else expressions
                    ? current[i]
                    : (byte) (current[i] - current[i - bpp]);

        return result;
    }

    static byte[] parseFilter(IHDR header, byte[] input) throws IOException {
        var lineLength = header.getLineLength();
        var bpp = header.getLineLength();

        var prevLine = new byte[lineLength];
        var output = ByteBuffer.allocate(header.width() * header.height() * bpp);

        System.out.println("input = " + Arrays.toString(input));

        for (var y = 0; y < header.height(); y++) {
            // Get the filter byte for the current row
            var filter = input[y * (lineLength + 1)];

            // Allocate space for the current row and copy the pixel data (skip the filter byte)
            var row = new byte[lineLength];
            System.arraycopy(input, 1 + y * (lineLength + 1), row, 0, lineLength);

            System.out.println("filter = " + filter);

            // Apply the correct filter based on the filter type
            switch (filter) {
                case 0 -> {} // No filter
                case 1 -> applySubFilter(row, bpp); // Sub filter
                case 2 -> applyUpFilter(row, prevLine); // Up filter
                case 3 -> applyAverageFilter(row, prevLine, bpp); // Average filter
                case 4 -> applyPaethFilter(row, prevLine, bpp); // Paeth filter
                default -> throw new IOException("The filter: " + filter + ", isn't recognized on line: " + y + ".");
            }

            // Store the processed row in the output buffer
            output.put(row);
            System.out.println("row " + y + " = " + Arrays.toString(row));

            // Update previous line for the next iteration
            prevLine = row;
        }
        return output.array();
    }

    // Each byte is the difference between the current byte and the byte to the left
    private static void applySubFilter(byte[] row, int bpp) {
        for (int i = 0; i < row.length; i++) {
            int left = (i >= bpp) ? (row[i - bpp] & 0xFF) : 0; // handle signed bytes
            row[i] = (byte)((row[i] & 0xFF) - left);
        }
    }
    // Each byte is the difference between the current byte and the byte above

    private static void applyUpFilter(byte[] row, byte[] previousRow) {
        for (int i = 0; i < row.length; i++) {
            int above = (previousRow != null) ? (previousRow[i] & 0xFF) : 0; // handle signed bytes
            row[i] = (byte)((row[i] & 0xFF) - above);
        }
    }
    // Each byte is the difference between the current byte and the average of the byte to the left and the byte above

    private static void applyAverageFilter(byte[] row, byte[] previousRow, int bpp) {
        for (int i = 0; i < row.length; i++) {
            int left = (i >= bpp) ? (row[i - bpp] & 0xFF) : 0; // handle signed bytes
            int above = (previousRow != null) ? (previousRow[i] & 0xFF) : 0;
            row[i] = (byte)((row[i] & 0xFF) - ((left + above) / 2));
        }
    }
    // Paeth filter: each byte is a function of the three neighboring pixels (left, above, and upper-left)

    private static void applyPaethFilter(byte[] row, byte[] previousRow, int bpp) {
        for (int i = 0; i < row.length; i++) {
            int left = (i >= bpp) ? (row[i - bpp] & 0xFF) : 0;
            int above = (previousRow != null) ? (previousRow[i] & 0xFF) : 0;
            int upperLeft = (previousRow != null && i >= bpp) ? (previousRow[i - bpp] & 0xFF) : 0;

            int predicted = paethPredictor(left, above, upperLeft);
            row[i] = (byte)((row[i] & 0xFF) - predicted);
        }
    }

    static int paethPredictor(int left, int above, int upperLeft) {
        int p = left + above - upperLeft;
        int pLeft = Math.abs(p - left);
        int pAbove = Math.abs(p - above);
        int pUpperLeft = Math.abs(p - upperLeft);

        if (pLeft <= pAbove && pLeft <= pUpperLeft) {
            return left;
        } else if (pAbove <= pUpperLeft) {
            return above;
        } else {
            return upperLeft;
        }
    }


}
