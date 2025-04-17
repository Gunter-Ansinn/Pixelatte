package net.ansinn.pixelatte.formats.png.layout;

import net.ansinn.pixelatte.formats.png.layout.chunks.Empty;

import java.util.HexFormat;

@SuppressWarnings("unused")
public interface Chunk {

    HexFormat format = HexFormat.of();
    byte[] NONE = new byte[0];

    enum ColorType {
        Grayscale(new byte[]{1, 2, 4, 8, 16}, 1),
        // Placeholder to align enum ordinal
        EMPTY(NONE, 0),

        TrueColor(new byte[]{8,16}, 3),
        Indexed(new byte[]{1,2,4,8}, 1),
        GreyscaleAlpha(new byte[]{8,16}, 2),

        // Placeholder to align enum ordinal with colortype
        EMPTY2(NONE, 0),

        TrueColorAlpha(new byte[]{8,16}, 4);

        private final byte[] allowedBitDepths;
        private final int channels;

        public static boolean isPLTECompatible(ColorType type) {
            return !(type == Grayscale || type == GreyscaleAlpha);
        }

        public boolean supportsBitDepth(byte depth) {
            for (var b : allowedBitDepths)
                if (b == depth) return true;
            return false;
        }

        public byte[] getAllowedBitDepths() {
            return allowedBitDepths;
        }

        public int getChannels() {
            return channels;
        }

        ColorType(byte[] allowedBitDepths, int channels) {
            this.allowedBitDepths = allowedBitDepths;
            this.channels = channels;
        }
    }

    record ColorData(byte red, byte green, byte blue) {

        public int uRed() {
            return red & 0xFF;
        }

        public int uGreen() {
            return green & 0xFF;
        }

        public int uBlue() {
            return blue & 0xFF;
        }


        public int[] data() {
            return new int[] {uRed(), uGreen(), uBlue()};
        }

        @Override
        public String toString() {
            return "Color{" +
                    "red=" + uRed() +
                    ", green=" + uGreen() +
                    ", blue=" + uBlue() +
                    '}';
        }
    }

}
