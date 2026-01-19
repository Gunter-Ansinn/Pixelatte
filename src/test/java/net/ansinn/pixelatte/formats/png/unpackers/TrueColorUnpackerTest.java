package net.ansinn.pixelatte.formats.png.unpackers;

import net.ansinn.pixelatte.DecodedImage8;
import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;
import net.ansinn.pixelatte.formats.png.layout.chunks.tRNS;
import net.ansinn.pixelatte.formats.png.unpackers.TrueColorUnpacker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrueColorUnpackerTest {

    @Test
    void testTrueColor8Bit() {
        var header = new IHDR(1,1, (byte) 8, Chunk.ColorType.TrueColor, (byte) 0, (byte) 0, (byte) 0);

        // Create our fake data blob composed of 3 bytes or 3 8 bit colors
        var filtered = new byte[] {
                0x12, 0x56, (byte) 0x9A
        };

        // Define our transparent pixel color then add it to the chunk map
        var chunkMap = new ChunkMap();

        var image = (DecodedImage8) TrueColorUnpacker.unpackTrueColor(filtered, header, chunkMap);
        var pixels = image.pixels();

        assertEquals((byte) 0x12, pixels[0], "Red");
        assertEquals((byte) 0x56, pixels[1], "Green");
        assertEquals((byte) 0x9A, pixels[2], "Blue");

    }

    @Test
    void testTrueColor16Bit() {
        var header = new IHDR(1,1, (byte) 16, Chunk.ColorType.TrueColor, (byte) 0, (byte) 0, (byte) 0);

        // Create our fake data blob composed of 6 bytes or 3 16 bit colors
        var filtered = new byte[] {
                0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC
        };

        // Define our transparent pixel color then add it to the chunk map
        var chunkMap = new ChunkMap();

        var image = (DecodedImage8) TrueColorUnpacker.unpackTrueColor(filtered, header, chunkMap);
        var pixels = image.pixels();

        assertEquals((byte) 0x12, pixels[0], "Red");
        assertEquals((byte) 0x56, pixels[1], "Green");
        assertEquals((byte) 0x9A, pixels[2], "Blue");

    }

    @Test
    void testTrueColor8BitTransparency() {
        var header = new IHDR(1,1, (byte) 8, Chunk.ColorType.TrueColor, (byte) 0, (byte) 0, (byte) 0);

        // Create our fake data blob composed of 6 bytes or 3 16 bit colors
        var filtered = new byte[] {
                0x12, 0x56, (byte) 0x9A
        };

        // Define our transparent pixel color then add it to the chunk map
        var tRNS = new tRNS.TrueColor(0x12, 0x56, 0x9A);
        var chunkMap = new ChunkMap();
        chunkMap.addChunk(tRNS);

        var image = (DecodedImage8) TrueColorUnpacker.unpackTrueColor(filtered, header, chunkMap);
        var pixels = image.pixels();

        assertEquals((byte) 0x12, pixels[0], "Red");
        assertEquals((byte) 0x56, pixels[1], "Green");
        assertEquals((byte) 0x9A, pixels[2], "Blue");
        assertEquals((byte) 0x00, pixels[3], "Alpha (should be transparent)");

    }

    @Test
    void testTrueColor16BitTransparency() {
        var header = new IHDR(1,1, (byte) 16, Chunk.ColorType.TrueColor, (byte) 0, (byte) 0, (byte) 0);

        // Create our fake data blob composed of 6 bytes or 3 16 bit colors
        var filtered = new byte[] {
                0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC
        };

        // Define our transparent pixel color then add it to the chunk map
        var tRNS = new tRNS.TrueColor(0x1234, 0x5678, 0x9ABC);
        var chunkMap = new ChunkMap();
        chunkMap.addChunk(tRNS);

        var image = (DecodedImage8) TrueColorUnpacker.unpackTrueColor(filtered, header, chunkMap);
        var pixels = image.pixels();

        assertEquals((byte) 0x12, pixels[0], "Red");
        assertEquals((byte) 0x56, pixels[1], "Green");
        assertEquals((byte) 0x9A, pixels[2], "Blue");
        assertEquals((byte) 0x00, pixels[3], "Alpha (should be transparent)");

    }

}
