package net.ansinn.pixelatte;

import net.ansinn.ByteBarista.SimpleRecordDecoder;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static net.ansinn.pixelatte.TestUtils.mapRes2File;

public class TestImageParse {

    @Test
    void loadImage() {

        mapRes2File("/png_tests/basn0g01.png")
                .ifPresent(TextureLoader::readFile);
    }

    @Test
    void testBufferReader() throws IllegalAccessException, NoSuchMethodException {
        var buffer = ByteBuffer.allocate(100);
        buffer.put((byte) 10);
        buffer.put((byte) 5);

        buffer.flip();
        var result = SimpleRecordDecoder.decodeRecord(buffer, test.class);
        System.out.println("result = " + result);
    }

    public record test(@SimpleRecordDecoder.UnsignedByte int number, byte b) {}

}
