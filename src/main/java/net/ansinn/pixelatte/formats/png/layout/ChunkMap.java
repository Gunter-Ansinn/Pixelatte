package net.ansinn.pixelatte.formats.png.layout;

import java.util.*;

public class ChunkMap {

    private final Map<Class<? extends Chunk>, List<Chunk>> chunkMap = new HashMap<>();

    public ChunkMap addChunk(Chunk chunk) {
        Objects.requireNonNull(chunk, "Chunks entered into the chunkmap cannot be null");
        chunkMap.computeIfAbsent(chunk.getClass(), chunks -> new ArrayList<>()).add(chunk);

        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Chunk> Optional<T> getFirst(Class<T> chunkType) {
        Objects.requireNonNull(chunkType, "Requested chunk type cannot be null");
        var list = chunkMap.get(chunkType);

        if (list == null || list.isEmpty())
            return Optional.empty();

        return Optional.ofNullable((T) list.getFirst());
    }

    @SuppressWarnings("unchecked")
    public <T extends Chunk> List<T> getAll(Class<T> chunkType) {
        Objects.requireNonNull(chunkType, "Requested chunk type cannot be null");
        var list = chunkMap.get(chunkType);

        if (list == null)
            return List.of();

        return (List<T>) Collections.unmodifiableList(list);
    }
}
