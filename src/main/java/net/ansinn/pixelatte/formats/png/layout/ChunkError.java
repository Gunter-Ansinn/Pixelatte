package net.ansinn.pixelatte.formats.png.layout;

import java.util.Arrays;

public record ChunkError(int tag, byte[] chunkData, String error) implements Chunk {

    public ChunkError {
        if (error.isBlank())
            throw new IllegalStateException("Chunk errors must have attached error reasons."); // Literally worthless without these
    }

    @Override
    public String toString() {
        return "ChunkError{" +
                "tag=" + tag +
                ", chunkData=" + Arrays.toString(chunkData) +
                ", error='" + error + '\'' +
                '}';
    }
}
