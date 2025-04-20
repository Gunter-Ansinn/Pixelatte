package net.ansinn.pixelatte.formats.png;

import net.ansinn.pixelatte.formats.png.layout.Chunk;

import java.util.ArrayList;

sealed interface ParsedChunkOutput {
    record ImageDataOnly(byte[] data) implements ParsedChunkOutput {}
    record ImageDataAndChunks(byte[] data, ArrayList<Chunk> chunks) {}
}
