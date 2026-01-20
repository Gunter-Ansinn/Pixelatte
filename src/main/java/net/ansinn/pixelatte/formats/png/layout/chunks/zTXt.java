package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.pixelatte.formats.png.layout.Chunk;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record zTXt(String keyword, byte compressionMethod, byte[] compressedText) implements Chunk {

    public static Chunk provider(ByteBuffer data, IHDR header) {
        int startPos = data.position();
        int nullPos = -1;
        
        for (int i = startPos; i < data.limit(); i++) {
            if (data.get(i) == 0) {
                nullPos = i;
                break;
            }
        }
        
        if (nullPos == -1)
            throw new IllegalArgumentException("zTXt chunk missing null separator");
        
        byte[] keyBytes = new byte[nullPos - startPos];
        data.get(keyBytes);
        String keyword = new String(keyBytes, StandardCharsets.ISO_8859_1);
        
        data.get(); // Skip null
        
        byte method = data.get();
        
        byte[] compressed = new byte[data.remaining()];
        data.get(compressed);
        
        return new zTXt(keyword, method, compressed);
    }
}
