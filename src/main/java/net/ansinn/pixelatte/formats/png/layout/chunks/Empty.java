package net.ansinn.pixelatte.formats.png.layout.chunks;


import net.ansinn.pixelatte.formats.png.layout.Chunk;

/**
 * Utility class to signify an empty chunk. It contains no data and serves merely as a marker.
 */
public class Empty implements Chunk {
    public static final Empty EMPTY = new Empty();
    private Empty() {}
}
