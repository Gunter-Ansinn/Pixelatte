package net.ansinn.pixelatte.unpackers;

import net.ansinn.pixelatte.DecodedImage8;
import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;
import net.ansinn.pixelatte.formats.png.layout.chunks.tRNS;
import net.ansinn.pixelatte.formats.png.unpackers.GrayscaleUnpacker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GrayscaleUnpackerTest {

    // Non transparent
    @Test
    void test1Bit() {
        // Create a fake header
        var header = new IHDR(8, 1, (byte) 1, Chunk.ColorType.Grayscale, (byte) 0, (byte) 0, (byte) 0);
        var filtered = new byte[] { (byte) 0b10101010 }; // Provide filtered scanline (already unfiltered!)
        var chunkMap = new ChunkMap();

        var image = (DecodedImage8) GrayscaleUnpacker.unpackGrayscale(filtered, header, chunkMap);

        var pixels = image.pixels();

        for (int i = 0; i < 8; i++) {
            int gray = (i % 2 == 0) ? 255 : 0;
            int offset = i * 4;

            assertEquals((byte) gray, pixels[offset],     "Red mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 1], "Green mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 2], "Blue mismatch at pixel " + i);
            assertEquals((byte) 255,  pixels[offset + 3], "Alpha mismatch at pixel " + i);
        }
    }

    @Test
    void test2Bit() {
        var header = new IHDR(4, 1, (byte) 2, Chunk.ColorType.Grayscale, (byte) 0, (byte) 0, (byte) 0);
        byte[] filtered = new byte[] { (byte) 0b00011011 };
        var chunkMap = new ChunkMap();

        var image = (DecodedImage8) GrayscaleUnpacker.unpackGrayscale(filtered, header, chunkMap);
        var pixels = image.pixels();
        var expected = new int[]{ 0, 85, 170, 255};

        for (int i = 0; i < 4; i++) {
            int offset = i * 4;
            int gray = expected[i];

            assertEquals((byte) gray, pixels[offset],     "Red mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 1], "Green mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 2], "Blue mismatch at pixel " + i);
            assertEquals((byte) 255,  pixels[offset + 3], "Alpha mismatch at pixel " + i);
        }

    }

    @Test
    void test4Bit() {
        var header = new IHDR(2, 1, (byte) 4, Chunk.ColorType.Grayscale, (byte) 0, (byte) 0, (byte) 0);
        var filtered = new byte[] { (byte) 0b00010001 }; // values: 0, 1 → grayscale: 0, 17
        var chunkMap = new ChunkMap();

        var image = (DecodedImage8)GrayscaleUnpacker.unpackGrayscale(filtered, header, chunkMap);
        var pixels = image.pixels();
        var expected = new int[]{ 17, 17 };

        for (int i = 0; i < 2; i++) {
            int offset = i * 4;
            int gray = expected[i];

            assertEquals((byte) gray, pixels[offset],     "Red mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 1], "Green mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 2], "Blue mismatch at pixel " + i);
            assertEquals((byte) 255,  pixels[offset + 3], "Alpha mismatch at pixel " + i);
        }
    }

    @Test
    void test8Bit() {
        var header = new IHDR(3, 1, (byte) 8, Chunk.ColorType.Grayscale, (byte) 0, (byte) 0, (byte) 0);
        byte[] filtered = new byte[] { (byte) 0, (byte) 127, (byte) 255 };
        var chunkMap = new ChunkMap();

        var image = (DecodedImage8)GrayscaleUnpacker.unpackGrayscale(filtered, header, chunkMap);
        var pixels = image.pixels();

        for (int i = 0; i < 3; i++) {
            int offset = i * 4;
            int gray = Byte.toUnsignedInt(filtered[i]);

            assertEquals((byte) gray, pixels[offset],     "Red mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 1], "Green mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 2], "Blue mismatch at pixel " + i);
            assertEquals((byte) 255,  pixels[offset + 3], "Alpha mismatch at pixel " + i);
        }
    }

    @Test
    void test16Bit() {
        var header = new IHDR(3, 1, (byte) 16, Chunk.ColorType.Grayscale, (byte) 0, (byte) 0, (byte) 0);
        // Values: 0x0000, 0x7F00, 0xFF00 → High byte becomes 0, 127, 255
        byte[] filtered = new byte[] {
                0x00, 0x00,
                0x7F, 0x00,
                (byte) 0xFF, 0x00
        };
        var chunkMap = new ChunkMap();

        var image = (DecodedImage8) GrayscaleUnpacker.unpackGrayscale(filtered, header, chunkMap);
        var pixels = image.pixels();
        int[] expected = { 0, 127, 255 };

        for (int i = 0; i < 3; i++) {
            int offset = i * 4;
            int gray = expected[i];

            assertEquals((byte) gray, pixels[offset],     "Red mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 1], "Green mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 2], "Blue mismatch at pixel " + i);
            assertEquals((byte) 255,  pixels[offset + 3], "Alpha mismatch at pixel " + i);
        }
    }

    // Transparent tests
    @Test
    void test8BitTransparency() {
        var header = new IHDR(3, 1, (byte) 8, Chunk.ColorType.Grayscale, (byte) 0, (byte) 0, (byte) 0);
        var filtered = new byte[] { (byte) 0, (byte) 127, (byte) 255 };

        // transparency defined for 127 → this pixel should have alpha = 0
        var transparency = new tRNS.Grayscale(127);
        var chunkMap = new ChunkMap();
        chunkMap.addChunk(transparency);

        var image = (DecodedImage8) GrayscaleUnpacker.unpackGrayscale(filtered, header, chunkMap);
        var pixels = image.pixels();

        for (int i = 0; i < 3; i++) {
            int offset = i * 4;
            int gray = Byte.toUnsignedInt(filtered[i]);
            int expectedAlpha = (gray == 127) ? 0 : 255;

            assertEquals((byte) gray, pixels[offset],     "Red mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 1], "Green mismatch at pixel " + i);
            assertEquals((byte) gray, pixels[offset + 2], "Blue mismatch at pixel " + i);
            assertEquals((byte) expectedAlpha, pixels[offset + 3], "Alpha mismatch at pixel " + i);
        }
    }

    @Test
    void test16BitTransparency() {
        var header = new IHDR(2, 1, (byte) 16, Chunk.ColorType.Grayscale, (byte) 0, (byte) 0, (byte) 0);

        var filtered = new byte[] {
                0x12, 0x34, // 0x1234 = transparent value
                0x56, 0x78  // ≠ 0x1234
        };

        var transparency = new tRNS.Grayscale(0x1234);
        var chunkMap = new ChunkMap();
        chunkMap.addChunk(transparency);

        var image = (DecodedImage8) GrayscaleUnpacker.unpackGrayscale(filtered, header, chunkMap);
        var pixels = image.pixels();

        // Expect: first pixel alpha = 0, second = 255
        var expectedGray = new int[]{ 0x12, 0x56 }; // high byte downscaled
        var expectedAlpha = new int[]{ 0, 255 };

        for (int i = 0; i < 2; i++) {
            int offset = i * 4;
            assertEquals((byte) expectedGray[i], pixels[offset],     "Red mismatch at pixel " + i);
            assertEquals((byte) expectedGray[i], pixels[offset + 1], "Green mismatch at pixel " + i);
            assertEquals((byte) expectedGray[i], pixels[offset + 2], "Blue mismatch at pixel " + i);
            assertEquals((byte) expectedAlpha[i], pixels[offset + 3], "Alpha mismatch at pixel " + i);
        }
    }
}
