package net.ansinn.pixelatte.output;

/**
 * This is the lowest possible root of what an image can be and it only aims to encapsulate three things.
 *
 * - What is the width of an image?
 * - What is the height of an image?
 * - What is the data on an image?
 */
public interface ImageMeta {

    int width();

    int height();

    ImageMeta copy();

    default int normalIndex(int x, int y) {
        return (y * width() + x);
    }

}
