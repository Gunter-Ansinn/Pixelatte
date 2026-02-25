package net.ansinn.pixelatte.formats.png.unpackers;

import net.ansinn.pixelatte.output.safe.StaticImage16;
import net.ansinn.pixelatte.output.safe.StaticImage8;
import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GrayscaleAlphaUnpackerTest {

    @Test
    void test8Bit() {
        var header = new IHDR(2, 1, (byte) 8, Chunk.ColorType.GreyscaleAlpha, (byte) 0, (byte) 0, (byte) 0);
        var filtered = new byte[] {
                (byte) 50, (byte) 255,
                (byte) 100, (byte) 128
        };

        var chunkMap = new ChunkMap();
        var image = (StaticImage8) GrayscaleAlphaUnpacker.unpackGrayscaleAlpha(filtered, header, chunkMap);
        var pixels = image.data();

        for (int i = 0; i < 2; i++) {
            int offset = i * 4;
            int gray = Byte.toUnsignedInt(filtered[i * 2]);
            int alpha = Byte.toUnsignedInt(filtered[i * 2 + 1]);

            assertEquals((byte) gray, pixels[offset],     "Red mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 1], "Green mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 2], "Blue mismatch at pixel " + i);
            assertEquals((byte) alpha,  pixels[offset + 3], "Alpha mismatch at pixel " + i);
        }
    }

    @Test
    void test16Bit() {
        var header = new IHDR(2, 1, (byte) 16, Chunk.ColorType.GreyscaleAlpha, (byte) 0, (byte) 0, (byte) 0);
        var filtered = new byte[] {
                0, (byte) 50, 0, (byte) 255,
                0, (byte) 100, 0, (byte) 128
        };

        var chunkMap = new ChunkMap();
        var image = (StaticImage16) GrayscaleAlphaUnpacker.unpackGrayscaleAlpha(filtered, header, chunkMap);
        var pixels = image.data();

        var expected = new short[] {
                50,50,50, 255,
                100,100,100,128
        };

        for (int i = 0; i < 2; i++) {
            int offset = i * 4;

            assertEquals(expected[offset],     pixels[offset],     "Red at pixel " + i);
            assertEquals(expected[offset + 1], pixels[offset + 1], "Green at pixel " + i);
            assertEquals(expected[offset + 2], pixels[offset + 2], "Blue at pixel " + i);
            assertEquals(expected[offset + 3], pixels[offset + 3], "Alpha at pixel " + i);
        }
    }
}
