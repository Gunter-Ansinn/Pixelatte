package net.ansinn.pixelatte.output;

import net.ansinn.pixelatte.formats.png.layout.ChunkMap;

public record DecodedImage8(int width, int height, byte[] pixels, Format format, ChunkMap chunkMap) implements DecodedImage {

    public int getRed(int x, int y) {
        return pixels[normalizeIndex(x, y)] & 0xFF;
    }

    public int getGreen(int x, int y) {
        return pixels[normalizeIndex(x, y) + 1] & 0xFF;
    }

    public int getBlue(int x, int y) {
        return pixels[normalizeIndex(x, y) + 2] & 0xFF;
    }

    public int getAlpha(int x, int y) {
        return pixels[normalizeIndex(x, y) + 3] & 0xFF;
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
        var result = new int[pixels.length / 4];

        for (var index = 0; index < result.length; index++) {
            var red = pixels[index * 4] & 0xFF;
            var green = pixels[index * 4 + 1] & 0xFF;
            var blue = pixels[index * 4 + 2] & 0xFF;
            var alpha = pixels[index * 4 + 3] & 0xFF;

            result[index] = (alpha << 24) | (red << 16) | (green << 8) | blue;
        }

        return result;
    }

    private int normalizeIndex(int x, int y) {
        return (y * width + x) * 4;
    }

}
