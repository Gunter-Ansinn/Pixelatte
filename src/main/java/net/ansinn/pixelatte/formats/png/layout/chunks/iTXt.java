package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.pixelatte.formats.png.layout.Chunk;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record iTXt(
        String keyword,
        boolean compressed,
        byte compressionMethod,
        String languageTag,
        String translatedKeyword,
        String text
) implements Chunk {

    public static Chunk provider(ByteBuffer data, IHDR header) {
        // Helper to read null-terminated string
        String keyword = readString(data, StandardCharsets.ISO_8859_1);
        
        boolean compressed = data.get() != 0;
        byte method = data.get();
        
        String langTag = readString(data, StandardCharsets.US_ASCII);
        String transKey = readString(data, StandardCharsets.UTF_8);
        
        byte[] remaining = new byte[data.remaining()];
        data.get(remaining);
        // iTXt text is always UTF-8 (after decompression)
        String textBody = new String(remaining, StandardCharsets.UTF_8); 
        
        return new iTXt(keyword, compressed, method, langTag, transKey, textBody);
    }
    
    private static String readString(ByteBuffer data, java.nio.charset.Charset charset) {
        int start = data.position();
        int end = -1;

        for (int i = start; i < data.limit(); i++) {
            if (data.get(i) == 0) {
                end = i;
                break;
            }
        }

        if (end == -1)
            throw new IllegalArgumentException("Missing null separator in iTXt");
        
        byte[] buf = new byte[end - start];
        data.get(buf);
        data.get(); // Skip null
        return new String(buf, charset);
    }
}
