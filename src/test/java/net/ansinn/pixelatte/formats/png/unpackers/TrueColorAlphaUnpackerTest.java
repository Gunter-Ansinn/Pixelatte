package net.ansinn.pixelatte.formats.png.unpackers;

import net.ansinn.pixelatte.output.safe.StaticImage16;
import net.ansinn.pixelatte.output.safe.StaticImage8;
import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrueColorAlphaUnpackerTest {

    @Test
    void textTrueColorAlpha8Bit() {
        var header = new IHDR(2,1, (byte) 8, Chunk.ColorType.TrueColorAlpha, (byte) 0, (byte) 0, (byte) 0);

        // Create our fake data blob composed of 8 bytes or 6 8 bit colors and two alpha values
        var filtered = new byte[] {
                (byte)255, 0, 0, (byte)128,  // Red with alpha 128
                0, (byte) 255, 0, (byte) 255, // Green with alpha 255
        };

        // Define our transparent pixel color then add it to the chunk map
        var chunkMap = new ChunkMap();
        var image = (StaticImage8) TrueColorAlphaUnpacker.unpackTrueColorAlpha(filtered, header, chunkMap);
        var pixels = image.data();

        for(var i = 0; i < 2; i++) {
            int inOffset = i * 4;

            assertEquals(Byte.toUnsignedInt(filtered[inOffset]),     Byte.toUnsignedInt(pixels[inOffset]),     "Red at pixel " + i);
            assertEquals(Byte.toUnsignedInt(filtered[inOffset + 1]), Byte.toUnsignedInt(pixels[inOffset + 1]), "Green at pixel " + i);
            assertEquals(Byte.toUnsignedInt(filtered[inOffset + 2]), Byte.toUnsignedInt(pixels[inOffset + 2]), "Blue at pixel " + i);
            assertEquals(Byte.toUnsignedInt(filtered[inOffset + 3]), Byte.toUnsignedInt(pixels[inOffset + 3]), "Alpha at pixel " + i);
        }
    }

    @Test
    void textTrueColorAlpha16Bit() {
        var header = new IHDR(2,1, (byte) 16, Chunk.ColorType.TrueColorAlpha, (byte) 0, (byte) 0, (byte) 0);

        // Create our fake data blob composed of 8 bytes or 6 8 bit colors and two alpha values
        var filtered = new byte[] {
                0, (byte)255, 0, 0, 0, 0, 0, (byte)128,  // Red with alpha 128
                0, 0, 0, (byte) 255, 0, 0, 0, (byte) 255, // Green with alpha 255
        };

        // Define our transparent pixel color then add it to the chunk map
        var chunkMap = new ChunkMap();
        var image = (StaticImage16) TrueColorAlphaUnpacker.unpackTrueColorAlpha(filtered, header, chunkMap);
        var pixels = image.data();

        var expected = new short[][] {
                {255, 0, 0, 128},
                {0, 255, 0, 255}
        };

        for(var i = 0; i < 2; i++) {
            int inOffset = i * 4;

            assertEquals(expected[i][0], pixels[inOffset],     "Red at pixel " + i);
            assertEquals(expected[i][1], pixels[inOffset + 1], "Green at pixel " + i);
            assertEquals(expected[i][2], pixels[inOffset + 2], "Blue at pixel " + i);
            assertEquals(expected[i][3], pixels[inOffset + 3], "Alpha at pixel " + i);
        }
    }

}
