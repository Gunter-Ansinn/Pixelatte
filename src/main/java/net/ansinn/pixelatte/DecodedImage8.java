package net.ansinn.pixelatte;

import net.ansinn.pixelatte.formats.png.layout.ChunkMap;

public record DecodedImage8(int width, int height, byte[] pixels, Format format, ChunkMap chunkMap) implements DecodedImage {
}
