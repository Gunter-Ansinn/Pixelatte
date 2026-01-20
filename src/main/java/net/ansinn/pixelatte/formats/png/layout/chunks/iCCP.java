package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.pixelatte.formats.png.layout.Chunk;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record iCCP(String profileName, byte compressionMethod, byte[] compressedProfile) implements Chunk {

    public static Chunk provider(ByteBuffer data, IHDR header) {
        int startPos = data.position();
        int nullPos = -1;
        
        for (int i = startPos; i < data.limit(); i++) {
            if (data.get(i) == 0) {
                nullPos = i;
                break;
            }
        }
        
        if (nullPos == -1) throw new IllegalArgumentException("iCCP chunk missing null separator");
        
        byte[] nameBytes = new byte[nullPos - startPos];
        data.get(nameBytes);

        // Profile names are ISO-8859-1
        String name = new String(nameBytes, StandardCharsets.ISO_8859_1);
        
        data.get(); // Skip null
        
        byte method = data.get();
        
        byte[] profile = new byte[data.remaining()];
        data.get(profile);
        
        return new iCCP(name, method, profile);
    }
}
