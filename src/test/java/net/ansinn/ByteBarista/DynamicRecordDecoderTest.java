package net.ansinn.ByteBarista;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Random;

class DynamicRecordDecoderTest {

    @Test
    void simpleDecoderTest() throws Throwable {
        var recordClazz = test.class;

        var nums = 40;
        var buffer = ByteBuffer.allocate((Integer.BYTES + Integer.BYTES + Float.BYTES) * nums);
        var random = new Random();

        for (var i = 0; i < nums; i++) {
            buffer.putInt(random.nextInt(0, 100000));
            buffer.putInt(random.nextInt(0, 100000));
            buffer.putFloat(random.nextFloat());
        }
        buffer.flip();


        for (var i = 0; i < nums; i++) {
            var result = SimpleRecordDecoder.decodeRecord(buffer, recordClazz);
            System.out.println("result[" + (i + 1) + "] = " + result);
        }

    }

    @Test
    void testClassGen() {
        var clazzName = "typeWrapper";
        var methodName = "name";

    }

    public record test(int a, int b, float c) {}
}