package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.pixelatte.formats.png.layout.Chunk;

import java.io.IOException;
import java.nio.ByteBuffer;

import static net.ansinn.pixelatte.formats.png.layout.Chunk.ColorType.isPLTECompatible;


public record PLTE(ColorData[] colors) implements Chunk {


    public static Chunk provider(final byte[] dataBytes, final IHDR headerChunk) {
        ColorData[] palette = new ColorData[dataBytes.length / 3];
        int paletteIndex = 0;

        if (!isPLTECompatible(headerChunk.colorType()))
            throw new RuntimeException(new IOException("Invalid PNG, the header specifies that this image isn't indexed but there's a palette chunk."));

        if ((dataBytes.length % 3) != 0)
            throw new RuntimeException(new IOException("Invalid PLTE chunk, the length of this chunk data is not divisible by three."));

        var dataBuffer = ByteBuffer.wrap(dataBytes);

        while (dataBuffer.hasRemaining()) try {
//            palette[paletteIndex++] = SimpleRecordDecoder.decodeRecord(dataBuffer, ColorData.class);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return new PLTE(palette);
    }

}
