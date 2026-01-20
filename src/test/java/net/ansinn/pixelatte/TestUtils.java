package net.ansinn.pixelatte;

import net.ansinn.pixelatte.output.DecodedImage8;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

public class TestUtils {

    public static Optional<File> mapRes2File(String path) {
        try(var resource = Main.class.getResourceAsStream(path)) {
            if (resource == null)
                throw new FileNotFoundException("Stream could not be created");

            var temp = File.createTempFile(path.split("/")[2] + " | ",".tmp");

            temp.deleteOnExit();

            try(var out = new FileOutputStream(temp)) {
                resource.transferTo(out);
            }

            return Optional.of(temp);
        } catch (IOException exception) {
            System.out.println("You suck at this.");
            exception.printStackTrace();
        }

        return Optional.empty();
    }

    public static BufferedImage toBufferedImage(DecodedImage8 image) {
        int width = image.width();
        int height = image.height();
        byte[] pixels = image.pixels();

        BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (y * width + x) * 4;
                int r = pixels[i] & 0xFF;
                int g = pixels[i + 1] & 0xFF;
                int b = pixels[i + 2] & 0xFF;
                int a = pixels[i + 3] & 0xFF;

                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                buffered.setRGB(x, y, argb);
            }
        }

        return buffered;
    }
}
