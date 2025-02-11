package net.ansinn.pixelatte;

import org.junit.jupiter.api.Test;

import static net.ansinn.pixelatte.TestUtils.mapRes2File;

public class TestImageParse {

    @Test
    void loadImage() {

        mapRes2File("/png_tests/basn0g01.png")
                .ifPresent(TextureLoader::readFile);
    }

}
