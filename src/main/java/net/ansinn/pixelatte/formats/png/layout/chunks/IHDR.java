package net.ansinn.pixelatte.formats.png.layout.chunks;


import net.ansinn.pixelatte.formats.png.layout.Chunk;

import java.nio.ByteBuffer;

public record IHDR(int width, int height, byte bitDepth, Chunk.ColorType colorType, byte compressionMethod, byte filterMethod,
                   byte interlacedMethod) implements Chunk {

    public IHDR {
        if (compressionMethod != 0)
            throw new IllegalStateException("Invalid PNG image compression method.");

        if (filterMethod != 0)
            throw new IllegalStateException("Invalid PNG filter method.");
    }

    ByteBuffer getOutputBuffer() {
        return ByteBuffer.allocate(width * height * calculateBytesPerPixel(colorType(), bitDepth()));
    }

    int calculateBytesPerPixel(ColorType type, byte bitDepth) {
        return switch (type) {
            case Grayscale -> switch (bitDepth) {
                case 1, 2, 4, 8 -> 1;
                case 16 -> 2;
                default -> throw new IllegalArgumentException("Invalid bit depth for Greyscale: " + bitDepth);
            };
            case EMPTY, EMPTY2 -> 0;
            case TrueColor -> (bitDepth / 8) * 3;
            case Indexed -> {
                if (bitDepth <= 8)
                    yield 1;
                throw new IllegalArgumentException("Invalid bit depth for Indexed color: " + bitDepth);
            }
            case GreyscaleAlpha -> (bitDepth / 8) * 2;
            case TrueColorAlpha -> (bitDepth / 8) * 4;
        };
    }

    public int getBytesPerPixel() {
        return calculateBytesPerPixel(colorType(), bitDepth());
    }

    public int getLineLength() {
        return width() * getBytesPerPixel();
    }

    @Override
    public String toString() {
        return "IHDR{" +
                "width=" + width +
                ", height=" + height +
                ", bitDepth=" + bitDepth +
                ", colorType=" + colorType +
                ", compressionMethod=" + compressionMethod +
                ", filterMethod=" + filterMethod +
                ", interlacedMethod=" + interlacedMethod +
                '}';
    }
}
