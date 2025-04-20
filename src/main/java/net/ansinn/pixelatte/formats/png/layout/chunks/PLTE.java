package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.pixelatte.formats.png.layout.Chunk;

import java.io.IOException;
import java.nio.ByteBuffer;

import static net.ansinn.pixelatte.formats.png.layout.Chunk.ColorType.isPLTECompatible;


public record PLTE(ColorData[] colors) implements Chunk {


    public static Chunk provider(final ByteBuffer buffer, final IHDR header) {
        var palette = new ColorData[buffer.remaining() / 3];
        var paletteIndex = 0;

        if (!isPLTECompatible(header.colorType()))
            throw new RuntimeException(new IOException("Invalid PNG, the header specifies that this image isn't indexed but there's a palette chunk."));

        if ((buffer.remaining() % 3) != 0)
            throw new RuntimeException(new IOException("Invalid PLTE chunk, the length of this chunk data is not divisible by three."));

        while (buffer.hasRemaining()) {
            var red = buffer.get();
            var green = buffer.get();
            var blue = buffer.get();

            palette[paletteIndex++] = new ColorData(red, green, blue);
        }

        return new PLTE(palette);
    }

}
