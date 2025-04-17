package net.ansinn.pixelatte;

import net.ansinn.ByteBarista.DynamicRecordDecoder;
import net.ansinn.ByteBarista.NumericHelpers;
import net.ansinn.ByteBarista.SimpleRecordDecoder;
import net.ansinn.ByteBarista.annotations.UnsignedByte;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.lang.invoke.LambdaConversionException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static net.ansinn.pixelatte.TestUtils.mapRes2File;

public class TestImageParse {

    @Test
    void loadImage() {

        mapRes2File("/png_tests/basn0g01.png")
                .ifPresent(TextureLoader::readFile);

        mapRes2File("/png_tests/basn0g02.png")
                .ifPresent(TextureLoader::readFile);
    }


}
