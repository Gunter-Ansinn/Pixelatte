package net.ansinn.pixelatte.formats.png.layout.chunks;


import net.ansinn.pixelatte.formats.png.layout.Chunk;

import java.util.Arrays;

public record IDAT(byte[] bytes) implements Chunk {

    @Override
    public String toString() {
        return "IDAT{" +
                "bytes=" + Arrays.toString(bytes) +
                '}';
    }
}
