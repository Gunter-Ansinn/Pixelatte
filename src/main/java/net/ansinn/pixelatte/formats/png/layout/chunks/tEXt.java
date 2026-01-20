package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.pixelatte.formats.png.layout.Chunk;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record tEXt(String keyword, String text) implements Chunk {

    public static Chunk provider(ByteBuffer data, IHDR header) {
        // Find null separator
        int startPos = data.position();
        int nullPos = -1;
        
        // Scan for null terminator
        for (int i = startPos; i < data.limit(); i++) {
            if (data.get(i) == 0) {
                nullPos = i;
                break;
            }
        }
        
        if (nullPos == -1) {
            throw new IllegalArgumentException("tEXt chunk missing null separator");
        }
        
        // Read Keyword
        byte[] keyBytes = new byte[nullPos - startPos];
        data.get(keyBytes);
        String keyword = new String(keyBytes, StandardCharsets.ISO_8859_1);
        
        // Skip null
        data.get(); 
        
        // Read Text
        byte[] textBytes = new byte[data.remaining()];
        data.get(textBytes);
        String text = new String(textBytes, StandardCharsets.ISO_8859_1);
        
        return new tEXt(keyword, text);
    }
}
