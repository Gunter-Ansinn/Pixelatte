package net.ansinn.pixelatte.output.safe;

public sealed interface StaticImage extends PixelResource permits StaticImage8, StaticImage16 {

    /**
     * Flips the image horizontally in-place.
     */
    void flipHorizontal();

    /**
     * Flips the image vertically in-place.
     */
    void flipVertical();

    /**
     * Flatten given decoded image into an 8 bit color channel variant of itself.
     * @return 8 bit variant of decoded image.
     */
    StaticImage8 flattenTo8Bit();

}
