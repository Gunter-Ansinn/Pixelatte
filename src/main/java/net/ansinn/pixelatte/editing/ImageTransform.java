package net.ansinn.pixelatte.editing;

import net.ansinn.pixelatte.output.ImageMeta;

import java.util.function.Consumer;

public class ImageTransform<T extends ImageMeta> {

    private final T image;

    private ImageTransform(T image) {
        this.image = image;
    }

    public ImageTransform<T> operation(Consumer<T> block) {
        //noinspection unchecked
        T imageCopy = (T) this.image.copy();

        block.accept(imageCopy);

        return new ImageTransform<>(imageCopy);
    }

    /**
     * @return The final, modified image after all operations.
     */
    public T unwrap() {
        return this.image;
    }

    public static <T extends ImageMeta> ImageTransform<T> of(T image) {
        return new ImageTransform<>(image);
    }

}
