package net.ansinn.pixelatte.output.safe;

import net.ansinn.pixelatte.formats.png.layout.ChunkMap;

import java.util.Arrays;

import static net.ansinn.pixelatte.output.safe.PixelResource.Format;

public record GrayScale8(
        int width,
        int height,
        byte[] data,
        Format format,
        ChunkMap chunkMap
) implements Grayscale, BitDepth.Bit8 {

    @Override
    public GrayScale8 copy() {
        byte[] dataCopy = Arrays.copyOf(this.data, this.data.length);
        return new GrayScale8(this.width, this.height, dataCopy, this.format, this.chunkMap);
    }

    @Override
    public float getLuminance(int x, int y) {
        return 0;
    }
}
