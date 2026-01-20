package net.ansinn.pixelatte.output;

import net.ansinn.pixelatte.formats.png.layout.ChunkMap;

public record DecodedImage16(int width, int height, short[] pixels, Format format, ChunkMap chunkMap) implements DecodedImage {
}
