package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.pixelatte.formats.png.layout.Chunk;
import java.nio.ByteBuffer;
import java.util.Arrays;

public record hIST(short[] frequencies) implements Chunk {
    
    public static Chunk provider(ByteBuffer data, IHDR header) {
        // hIST must contain one 2-byte entry for each palette entry.
        // Since we don't strictly enforce PLTE presence here (ancillary order), 
        // we just read all available shorts.
        int entries = data.remaining() / 2;
        short[] frequencies = new short[entries];
        
        for (int i = 0; i < entries; i++) {
            frequencies[i] = data.getShort();
        }
        
        return new hIST(frequencies);
    }
    
    @Override
    public String toString() {
        return "hIST{entries=" + frequencies.length + "}";
    }
}
