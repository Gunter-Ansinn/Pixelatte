package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.pixelatte.formats.png.layout.Chunk;

public record sRGB(@UnsignedByte short renderingIntent) implements Chunk {

    public sRGB {
        if (renderingIntent < 0 || renderingIntent > 3)
            throw new IllegalArgumentException("Invalid sRGB rendering intent: " + renderingIntent);
    }

}
