package net.ansinn.pixelatte.formats.png.layout.chunks;


import net.ansinn.pixelatte.formats.png.ChunkRegistry;
import net.ansinn.pixelatte.formats.png.layout.Chunk;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public record IHDR(int width, int height, byte bitDepth, Chunk.ColorType colorType, byte compressionMethod, byte filterMethod,
                   byte interlacedMethod) implements Chunk {

    private static final Set<Byte> VALID_DEPTHS = Set.of((byte)1, (byte)2, (byte)4, (byte)8, (byte)16);


    public IHDR {
        if (width <= 0)
            throw new IllegalArgumentException("Width cannot be less than 1");

        if (height <= 0)
            throw new IllegalArgumentException("Height cannot be less than 1");

        if (!VALID_DEPTHS.contains(bitDepth))
            throw new IllegalArgumentException("Bit-depth cannot be any number but: ");

        Objects.requireNonNull(colorType, "ColorType cannot be null");

        if (compressionMethod != 0)
            throw new IllegalStateException("Invalid PNG image compression method.");

        if (filterMethod != 0)
            throw new IllegalStateException("Invalid PNG filter method.");

        if (interlacedMethod < 0 || interlacedMethod > 1)
            throw new IllegalArgumentException("Invalid interlace method: " + interlacedMethod);

        if (!containsByte(colorType.getAllowedBitDepths(), bitDepth))
            throw new IllegalArgumentException("Invalid bit depth: " + bitDepth + " for color type: " + colorType.name() + " allowed bit-depths are: " + Arrays.toString(colorType.getAllowedBitDepths()));
    }

    private static boolean containsByte(byte[] array, byte target) {
        for (var b : array) {
            if (b == target) {
                return true;
            }
        }
        return false;
    }

    private static int calculateBytesPerPixel(ColorType type, byte bitDepth) {
        if (type == ColorType.EMPTY || type == ColorType.EMPTY2)
            throw new IllegalArgumentException("Cannot calculate bytes per pixel for invalid ");

        var bytesPerSample = (bitDepth + 7) / 8;
        return type.getChannels() * bytesPerSample;
    }

    public int getBitsPerPixel() {
        return bitDepth * colorType().getChannels();
    }

    public int getBytesPerPixel() {
        return calculateBytesPerPixel(colorType(), bitDepth());
    }

    public int getScanlineByteLength() {
        var totalBits = width * getBitsPerPixel();
        return (totalBits + 7) / 8;
    }

    public boolean preferSIMD() {
        return getScanlineByteLength() >= 128 && bitDepth >= 8;
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
