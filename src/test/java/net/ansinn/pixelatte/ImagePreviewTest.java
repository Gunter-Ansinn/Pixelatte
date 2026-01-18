package net.ansinn.pixelatte;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static net.ansinn.pixelatte.TestUtils.mapRes2File;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImagePreviewTest {

    @Test
    void previewAll() throws Exception {

        var result = mapRes2File("/png_tests/basn0g08.png")
                .map(TextureLoader::readFile);


        result.ifPresent(decodedImage -> {

            if (!(decodedImage instanceof DecodedImage8 image)) {
                System.err.println("Not valid to be previewed");
                return;
            }

            var buffered = TestUtils.toBufferedImage(image).getScaledInstance(
                    image.width() * 8,
                    image.height() * 8,
                    Image.SCALE_DEFAULT
            );

            var frame = new JFrame("Pixelatte PNG previewer");

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new JLabel(new ImageIcon(buffered)));

            frame.setSize(500, 500);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        Thread.sleep(100000);
        assertTrue(true);
    }

}
