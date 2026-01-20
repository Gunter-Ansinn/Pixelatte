package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.pixelatte.formats.png.layout.Chunk;

public record cHRM(
        @UnsignedInteger long whitePointX,
        @UnsignedInteger long whitePointY,
        @UnsignedInteger long redX,
        @UnsignedInteger long redY,
        @UnsignedInteger long greenX,
        @UnsignedInteger long greenY,
        @UnsignedInteger long blueX,
        @UnsignedInteger long blueY) implements Chunk {
}
