package net.ansinn.pixelatte.output.safe;

import net.ansinn.pixelatte.formats.png.layout.ChunkMap;

import java.util.Arrays;

public record StaticImage8(
        int width,
        int height,
        byte[] data,
        Format format,
        ChunkMap chunkMap
) implements StaticImage, BitDepth.Bit8 {

    public int getRed(int x, int y) {
        return data[normalizeIndex(x, y)] & 0xFF;
    }

    public int getGreen(int x, int y) {
        return data[normalizeIndex(x, y) + 1] & 0xFF;
    }

    public int getBlue(int x, int y) {
        return data[normalizeIndex(x, y) + 2] & 0xFF;
    }

    public int getAlpha(int x, int y) {
        return data[normalizeIndex(x, y) + 3] & 0xFF;
    }

    public int getRGBA(int x, int y) {
        var red = getRed(x, y);
        var green = getGreen(x, y);
        var blue = getBlue(x, y);
        var alpha = getAlpha(x, y);

        return (red << 24) | (green << 16) | (blue << 8) | alpha;
    }

    public int getARGB(int x, int y) {
        var red = getRed(x, y);
        var green = getGreen(x, y);
        var blue = getBlue(x, y);
        var alpha = getAlpha(x, y);

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public int[] rgbaBytesToArgbInts() {
        var result = new int[data.length / 4];

        for (var index = 0; index < result.length; index++) {
            var red = data[index * 4] & 0xFF;
            var green = data[index * 4 + 1] & 0xFF;
            var blue = data[index * 4 + 2] & 0xFF;
            var alpha = data[index * 4 + 3] & 0xFF;

            result[index] = (alpha << 24) | (red << 16) | (green << 8) | blue;
        }

        return result;
    }

    private int normalizeIndex(int x, int y) {
        return (y * width + x) * 4;
    }

    @Override
    public void flipHorizontal() {
        final int channels = 4;
        int rowStride = width * channels;
        byte[] tempPixel = new byte[channels];
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
        byte[] tempRow = new byte[rowStride];
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
    public StaticImage8 copy() {
        byte[] dataCopy = Arrays.copyOf(this.data, this.data.length);
        return new StaticImage8(this.width, this.height, dataCopy, this.format, this.chunkMap);
    }

    @Override
    public StaticImage8 flattenTo8Bit() {
        return this;
    }
}
