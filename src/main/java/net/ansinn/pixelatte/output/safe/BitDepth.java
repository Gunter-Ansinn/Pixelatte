package net.ansinn.pixelatte.output.safe;

public sealed interface BitDepth {

    byte getDepth();

    sealed interface Bit8 extends BitDepth permits AnimatedImage8, GrayScale8, StaticImage8 {

        byte[] data();

        @Override
        default byte getDepth() {
            return 8;
        }
    }

    sealed interface Bit16 extends BitDepth permits StaticImage16, AnimatedImage16 {

        short[] data();

        @Override
        default byte getDepth() {
            return 16;
        }
    }
}
