package net.ansinn.pixelatte.formats.png.layout;

import net.ansinn.pixelatte.formats.png.layout.chunks.Empty;

import java.util.HexFormat;

@SuppressWarnings("unused")
public interface Chunk {

    HexFormat format = HexFormat.of();

    enum ColorType {
        Grayscale(new byte[]{1, 2, 4, 8, 16}),
        EMPTY(new byte[]{}),
        TrueColor(new byte[]{8,16}),
        Indexed(new byte[]{1,2,4,8}),
        GreyscaleAlpha(new byte[]{8,16}),
        EMPTY2(new byte[]{}),
        TrueColorAlpha(new byte[]{8,16});

        private final byte[] allowedBitDepths;

        public static boolean isPLTECompatible(ColorType type) {
            return !(type == Grayscale || type == GreyscaleAlpha);
        }

        public byte[] getAllowedBitDepths() {
            return allowedBitDepths;
        }

        ColorType(byte[] allowedBitDepths) {
            this.allowedBitDepths = allowedBitDepths;
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
