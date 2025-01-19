package net.ansinn.pixelatte.formats.png.layout.chunks;


import net.ansinn.pixelatte.formats.png.layout.Chunk;

public sealed interface tRNS extends Chunk {

    static Chunk provider(byte[] data, IHDR header) {
        return switch (header.colorType()) {
            case Grayscale -> new tRNSGrayscale();
            case Indexed -> new tRNSIndexed(data); // No processing needed, it's already as-is
            default -> Empty.EMPTY;
        };
    }

    record tRNSGrayscale() implements Chunk {}

    /**
     * represents a tRNS chunk variant for image type of indexed.
     * @param alpha indexed
     */
    record tRNSIndexed(byte[] alpha) implements tRNS {

        public tRNSIndexed {
            if (alpha.length == 0)
                throw new IllegalStateException("Tried to initialize tRNSIndexed chunk with zero length array.");
        }

        /**
         * Gets a singular unsigned alpha value from the tRNS chunk
         * @param index index of alpha value
         * @return value between 0-255 if successful -1 if error
         */
        int unsignedAlpha(int index) {

            if (index <= alpha().length - 1 && alpha().length > 0)
                return Byte.toUnsignedInt(alpha[index]);

            return -1;
        }
    }



}
