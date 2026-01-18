package net.ansinn.pixelatte;

import net.ansinn.ByteBarista.DynamicRecordDecoder;
import net.ansinn.ByteBarista.NumericHelpers;
import net.ansinn.ByteBarista.SimpleRecordDecoder;
import net.ansinn.ByteBarista.annotations.UnsignedByte;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.invoke.LambdaConversionException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static net.ansinn.pixelatte.TestUtils.mapRes2File;
import static net.ansinn.pixelatte.TestUtils.toBufferedImage;
import static org.junit.jupiter.api.Assertions.*;

public class TestImageParse {

    @Test
    void loadImage() {
        System.out.println("Hello?");

        var result = mapRes2File("/png_tests/basn0g01.png");
        System.out.println("Hello2");

        if (result.isPresent()) {
            System.out.println("we are IN");

            try {
                var texture = TextureLoader.readFile(result.get());
                System.out.println("Hello post tex");
                assertNotNull(texture);
                System.out.println(texture);

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }


    }

    @Test
    void compareAgainstImageIO() throws Exception {
        URL url = getClass().getResource("/png_tests");
        assertNotNull(url, "Resource folder '/png_tests' was not found.");

        Path folder = Path.of(url.toURI());
        Files.walk(folder)
            .sorted()
            .filter(path -> path.toString().endsWith(".png"))
            .forEach(path -> {
                System.out.println("Testing: " + path.getFileName());

                try {
                    File file = path.toFile();

                    // Decode with Pixelatte
                    var decoded = (DecodedImage8) TextureLoader.readFile(file);
                    assertNotNull(decoded);

                    // Decode with ImageIO
                    BufferedImage expected = ImageIO.read(file);

                    assertNotNull(expected, "ImageIO failed to load: " + path.getFileName());

                    // Compare size
                    assertEquals(expected.getWidth(), decoded.width(), "Width mismatch: " + path.getFileName());
                    assertEquals(expected.getHeight(), decoded.height(), "Height mismatch: " + path.getFileName());

                    // Compare pixels
                    int width = expected.getWidth();
                    int height = expected.getHeight();

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int exp = expected.getRGB(x, y);
                            int act = decoded.getARGB(x, y);

                            if (exp != act) {
                                fail("Mismatch at (" + x + ", " + y + ") in " + path.getFileName() +
                                        "\nExpected: " + Integer.toHexString(exp) +
                                        "\nActual:   " + Integer.toHexString(act));
                            }
                        }
                    }

                } catch (Throwable t) {
                    t.printStackTrace();
                    fail("Exception for file: " + path.getFileName() + " — " + t.getMessage());
                }
            });
    }

}
