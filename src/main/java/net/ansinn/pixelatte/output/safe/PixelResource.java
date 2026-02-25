package net.ansinn.pixelatte.output.safe;

import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.output.ImageMeta;

/**
 * The interface for dealing with safe, entirely GC-handled images.
 */
public sealed interface PixelResource extends ImageMeta permits AnimatedImage, StaticImage {

    Format format();
    ChunkMap chunkMap();

    enum Format {
        RGBA8(4), RGB8(3), GRAY8(1),
        RGBA16(4), RGB16(3), GRAY16(1);

        private final int channels;

        Format(int channels) {
            this.channels = channels;
        }

        public int channels() {
            return channels;
        }
    }

    /**
     * Returns a static variant of the image. Animated images passed through this method will
     * pass their thumbnail image in place of their animation.
     * @return a static image
     */
    default StaticImage asStatic() {
        return switch (this) {
            case AnimatedImage<?> animatedImage -> animatedImage.thumbnail();
            case StaticImage staticImage -> staticImage;
        };
    }

    /**
     * Returns an animated image if the image can be animated. Non-animated images will throw an UnsupportedOperationException.
     * This is in an effort to make this a very intentional cast of a known image.
     * @return an animated image
     */
    default AnimatedImage<? extends StaticImage> asAnimated() {
        return switch (this) {
            case AnimatedImage<?> animatedImage -> animatedImage;
            case StaticImage _ ->
                    throw new UnsupportedOperationException("Resource is not an animation");
        };
    }

}
