package net.ansinn.pixelatte.comparisons;

import net.ansinn.pixelatte.DecodedImage;
import net.ansinn.pixelatte.DecodedImage8;
import net.ansinn.pixelatte.Main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Optional;

/**
 * A helper class containing methohds to assist in debugging image loading
 */
public final class ComparisonUtils {

    ComparisonUtils() {}

    public static Optional<BufferedImage> getImage(String path) {
        try(var resource = Main.class.getResourceAsStream(path)) {
            if (resource == null)
                throw new IllegalStateException("invalid path");

            return Optional.of(ImageIO.read(resource));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void printImage(BufferedImage image) {
        var width = image.getWidth();
        var height = image.getHeight();

        var pixels = image.getRGB(0,0,width,height, null, 0, width);

        System.out.println("pixels = " + Arrays.toString(pixels));
    }

    public static void printImage(DecodedImage8 image) {

    }
}
