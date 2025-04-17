package net.ansinn.pixelatte.formats.png;


import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

public final class BitNibbler {

    /**
     * Chop up the unsigned bytes.
     * @param input
     * @param header
     * @return
     */
    public static NibbledOutput parseInput(int[] input, IHDR header) {
        return switch (header.bitDepth()) {
            case 1 -> getBits(input, header);
            case 2 -> getBitPair(input);
            case 4 -> getNibbles(input);
            case 8 -> getBytes(input);
            case 16 -> getShorts(input);
            default -> throw new IllegalStateException("Bit depth is out of bounds at: " + header.bitDepth() + ", check your parsing.");
        };
    }

    /**
     * Returns an array of bits in byte form in the following format:
     * <p></p>
     * <p>byte bit bit</p>
     * <p>byte bit bit</p>
     * <p></p>
     *
     * Where the byte is the filter and also first entry within the array and the following bytes are the data composed of bit values.
     *
     * @param input an array of unsigned bytes (as integers)
     * @param header the header data of the image
     * @return a new Bits object which contains the data in byte form.
     */
    private static NibbledOutput.Bits getBits(int[] input, IHDR header) {
        final var width = header.width();
        final var height = header.height();
        final var rowSize = (width + 7) / 8 + 1; // Each row includes the filter byte and packed pixels.
        final var totalBits = height * (1 + width); // 1 filter byte + 1 bit per pixel for each row.
        final var out = new byte[totalBits]; // Output array to store filter byte + bits for all rows.

        int outIndex = 0; // Tracks position in the output array.

        for (int row = 0; row < height; row++) {
            int rowStart = row * rowSize;
            int filterByte = input[rowStart];
            out[outIndex++] = (byte) filterByte; // Add the filter byte to the output.

            // Extract bits from the pixel data.
            for (int col = 0; col < width; col++) {
                int byteIndex = (col / 8) + 1; // Pixel bytes start after the filter byte.
                int bitIndex = 7 - (col % 8); // MSB to LSB within each byte.
                int currentByte = input[rowStart + byteIndex];

                // Use getBit to extract the specific bit.
                int bitValue = getBit(currentByte, bitIndex);
                out[outIndex++] = (byte) bitValue; // Store the bit (0 or 1).
            }
        }

        return new NibbledOutput.Bits(out);
    }

    private static NibbledOutput.BitPairs getBitPair(int[] input) {
        return new NibbledOutput.BitPairs(new byte[0]);
    }

    private static NibbledOutput.Nibbles getNibbles(int[] input) {
        return new NibbledOutput.Nibbles(new byte[0]);
    }

    private static NibbledOutput.Bytes getBytes(int[] input) {
        return new NibbledOutput.Bytes(input);
    }

    private static NibbledOutput.Integers getShorts(int[] input) {
        return new NibbledOutput.Integers(input);
    }

    public static int getBit(int number, int bit) {
        return (number >> bit) & 1;
    }

    private static int mergeBytes(byte chunk1, byte chunk2) {
        return 0;
    }

}
