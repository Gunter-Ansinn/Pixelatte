package net.ansinn.pixelatte.formats.png.layout.larval;

import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.chunks.IDAT;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * A larval form of an IDAT chunk.
 * @param data stored color data of chunk
 */
public record LarvalIDAT(byte[] data) implements Chunk {

    @Override
    public boolean isLarval() {
        return true;
    }

    public static Chunk provider(final byte[] dataBytes, final IHDR headerChunk) {
        return new LarvalIDAT(dataBytes);
    }

    public static Chunk processor(List<Chunk> chunks) {

        var outputStream = new ByteArrayOutputStream();

        for (var chunk : chunks) if (chunk instanceof LarvalIDAT idat){
            outputStream.writeBytes(idat.data());
        }

        return new IDAT(outputStream.toByteArray());
    }
}
