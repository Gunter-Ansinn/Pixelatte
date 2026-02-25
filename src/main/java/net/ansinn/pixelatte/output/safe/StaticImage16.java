package net.ansinn.pixelatte.output.safe;

import net.ansinn.pixelatte.formats.png.layout.ChunkMap;

import java.util.Arrays;

public record StaticImage16(
        int width,
        int height,
        short[] data,
        Format format,
        ChunkMap chunkMap
) implements StaticImage, BitDepth.Bit16 {

    @Override
    public void flipHorizontal() {
        final int channels = 4;
        int rowStride = width * channels;
        short[] tempPixel = new short[channels];
        for (int y = 0; y < height; y++) {
            int rowOffset = y * rowStride;
            for (int x1 = 0; x1 < width / 2; x1++) {
                int x2 = width - 1 - x1;
                int idx1 = rowOffset + x1 * channels;
                int idx2 = rowOffset + x2 * channels;

                // temp = pixel1
                System.arraycopy(data, idx1, tempPixel, 0, channels);
                // pixel1 = pixel2
                System.arraycopy(data, idx2, data, idx1, channels);
                // pixel2 = temp
                System.arraycopy(tempPixel, 0, data, idx2, channels);
            }
        }
    }

    @Override
    public void flipVertical() {
        final int channels = 4;
        int rowStride = width * channels;
        short[] tempRow = new short[rowStride];
        for (int y1 = 0; y1 < height / 2; y1++) {
            int y2 = height - 1 - y1;
            int row1Offset = y1 * rowStride;
            int row2Offset = y2 * rowStride;

            // temp = row1
            System.arraycopy(data, row1Offset, tempRow, 0, rowStride);
            // row1 = row2
            System.arraycopy(data, row2Offset, data, row1Offset, rowStride);
            // row2 = temp
            System.arraycopy(tempRow, 0, data, row2Offset, rowStride);
        }
    }

    @Override
    public StaticImage16 copy() {
        short[] dataCopy = Arrays.copyOf(this.data, this.data.length);
        return new StaticImage16(this.width, this.height, dataCopy, this.format, this.chunkMap);
    }

    @Override
    public StaticImage8 flattenTo8Bit() {
        int len = data.length;
        byte[] newPixels = new byte[len];

        for (int i = 0; i < len; i++) {
            // Read short (handle signedness if needed, but usually logical shift works)
            // Java short is signed, so (data[i] & 0xFFFF) >>> 8 ensures unsigned shift behavior
            newPixels[i] = (byte) ((data[i] & 0xFFFF) >>> 8);
        }

        return new StaticImage8(width, height, newPixels, format, chunkMap);
    }

}
