package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.pixelatte.formats.png.layout.Chunk;

import static net.ansinn.pixelatte.formats.png.ChunkRegistry.toTag;

public record acTL(@UnsignedInteger long frameCount, @UnsignedInteger long playCount) implements Chunk {

    public acTL {
        if (frameCount <= 0)
            throw new IllegalStateException("Zero frames are an invalid frame count for the fcTL chunk");
    }

    boolean isIndefinite() {
        return playCount == 0;
    }

}
