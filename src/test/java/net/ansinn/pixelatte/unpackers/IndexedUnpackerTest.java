package net.ansinn.pixelatte.unpackers;

import net.ansinn.pixelatte.DecodedImage8;
import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;
import net.ansinn.pixelatte.formats.png.layout.chunks.PLTE;
import net.ansinn.pixelatte.formats.png.unpackers.IndexedUnpacker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexedUnpackerTest {

    @Test
    void testIndexed1Bit() {
        var header = new IHDR(8, 1, (byte) 1, Chunk.ColorType.Indexed, (byte) 0, (byte) 0, (byte) 0);

        // Create a fake filtered buffer
        // Palette: 0b10101010 or 1,0,1,0,1,0,1,0
        // So it should be red green red green etc...
        byte[] filtered = new byte[] { (byte) 0b10101010 };

        var palette = new PLTE(new Chunk.ColorData[]{
                new Chunk.ColorData((byte) 255, (byte) 0, (byte) 0), // red
                new Chunk.ColorData((byte) 0, (byte) 255, (byte) 0) // green
        });

        var chunkMap = new ChunkMap().addChunk(palette);
        var image = (DecodedImage8) IndexedUnpacker.unpackIndexed(filtered, header, chunkMap);
        var pixels = image.pixels();

        for (int index = 0; index < 8; index++) {
            var offset = index * 4;
            var isEven = (index % 2 == 0);

            // Simple value selection
            var red = isEven ? (byte) 0 : (byte) 255;
            var green = isEven ? (byte) 255 : (byte) 0;

            assertEquals(red, pixels[offset],     "Red at pixel " + index);
            assertEquals(green, pixels[offset + 1], "Green at pixel " + index);
            assertEquals(0, pixels[offset + 2], "Blue at pixel " + index);
            assertEquals((byte) 255, pixels[offset + 3], "Alpha at pixel " + index);
        }
    }

    @Test
    void testIndexed2Bit() {
        var header = new IHDR(4, 1, (byte) 2, Chunk.ColorType.Indexed, (byte) 0, (byte) 0, (byte) 0);

        // [0, 1, 2, 3]
        var filtered = new byte[] { (byte) 0b00011011 };

        var palette = new PLTE(new Chunk.ColorData[]{
                new Chunk.ColorData((byte) 10, (byte) 0, (byte) 0),   // index 0 dark red
                new Chunk.ColorData((byte) 0, (byte) 10, (byte) 0),   // index 1 dark green
                new Chunk.ColorData((byte) 0, (byte) 0, (byte) 10),   // index 2 dark blue
                new Chunk.ColorData((byte) 10, (byte) 10, (byte) 10)  // index 3 gray
        });

        var chunkMap = new ChunkMap().addChunk(palette);
        var image = (DecodedImage8) IndexedUnpacker.unpackIndexed(filtered, header, chunkMap);
        var pixels = image.pixels();

        for (var i = 0; i < 4; i++) {
            var offset = i * 4;
            var color = palette.colors()[i];

            assertEquals(color.red(),   pixels[offset],     "Red mismatch at pixel " + i);
            assertEquals(color.green(), pixels[offset + 1], "Green mismatch at pixel " + i);
            assertEquals(color.blue(),  pixels[offset + 2], "Blue mismatch at pixel " + i);
            assertEquals((byte) 255,    pixels[offset + 3], "Alpha mismatch at pixel " + i);
        }
    }

    @Test
    void testIndexed4Bit() {
        var header = new IHDR(2, 1, (byte) 4, Chunk.ColorType.Indexed, (byte) 0, (byte) 0, (byte) 0);

        // [1, 0]
        var filtered = new byte[] { (byte) 0b00010000 };

        var palette = new PLTE(new Chunk.ColorData[]{
                new Chunk.ColorData((byte) 50, (byte) 50, (byte) 50),  // index 0 dark gray
                new Chunk.ColorData((byte) 100, (byte) 0, (byte) 0)    // index 1 medium red
        });

        var chunkMap = new ChunkMap().addChunk(palette);
        var image = (DecodedImage8) IndexedUnpacker.unpackIndexed(filtered, header, chunkMap);
        var pixels = image.pixels();

        var expected = new Chunk.ColorData[]{ palette.colors()[1], palette.colors()[0] };

        for (var i = 0; i < 2; i++) {
            var offset = i * 4;
            var color = expected[i];

            assertEquals(color.red(),   pixels[offset],     "Red mismatch at pixel " + i);
            assertEquals(color.green(), pixels[offset + 1], "Green mismatch at pixel " + i);
            assertEquals(color.blue(),  pixels[offset + 2], "Blue mismatch at pixel " + i);
            assertEquals((byte) 255,    pixels[offset + 3], "Alpha mismatch at pixel " + i);
        }
    }

    @Test
    void testIndexed8Bit() {
        var header = new IHDR(3, 1, (byte) 8, Chunk.ColorType.Indexed, (byte) 0, (byte) 0, (byte) 0);

        // [1, 0]
        var filtered = new byte[] { 0, 1, 2 };

        var palette = new PLTE(new Chunk.ColorData[]{
                new Chunk.ColorData((byte) 10, (byte) 0, (byte) 0),    // index 0 red
                new Chunk.ColorData((byte) 0, (byte) 10, (byte) 0),    // index 1 green
                new Chunk.ColorData((byte) 0, (byte) 0, (byte) 10)     // index 2 blue
        });

        var chunkMap = new ChunkMap().addChunk(palette);
        var image = (DecodedImage8) IndexedUnpacker.unpackIndexed(filtered, header, chunkMap);
        var pixels = image.pixels();

        for (var i = 0; i < 2; i++) {
            var offset = i * 4;
            var paletteIndex = filtered[i] & 0xFF;
            var color = palette.colors()[paletteIndex];

            assertEquals(color.red(),   pixels[offset],     "Red mismatch at pixel " + i);
            assertEquals(color.green(), pixels[offset + 1], "Green mismatch at pixel " + i);
            assertEquals(color.blue(),  pixels[offset + 2], "Blue mismatch at pixel " + i);
            assertEquals((byte) 255,    pixels[offset + 3], "Alpha mismatch at pixel " + i);
        }
    }
}
