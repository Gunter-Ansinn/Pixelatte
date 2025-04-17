package net.ansinn.pixelatte.formats.png.layout;

public record RawChunk(int chunkType, byte[] data, long crc) implements Chunk {
}
