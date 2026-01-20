package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.pixelatte.formats.png.layout.Chunk;

public record pHYs(
        @UnsignedInteger long pixelsPerUnitX,
        @UnsignedInteger long pixelsPerUnitY,
        @UnsignedByte short unitSpecifier
) implements Chunk {

    public boolean isMeters() {
        return unitSpecifier == 1;
    }

}
