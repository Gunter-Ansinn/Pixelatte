package net.ansinn.pixelatte.formats.png;

import net.ansinn.pixelatte.IntermediaryImage;
import net.ansinn.pixelatte.formats.png.layout.Chunk;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

public final class PNGParser {

    private PNGParser() {}

    /**
     *
     * @param inputBuffer the input buffer data
     * @return
     */
    public static IntermediaryImage parse(ByteBuffer inputBuffer) {
        //Make sure input stream isn't null
        Objects.requireNonNull(inputBuffer, "The input buffer is null");

        /*
            We can make conservative assumptions on needed buffer space for chunk type plus length.
            Max Length is 4 bytes like IHDR each character represented by one byte
            See the spec here: https://www.w3.org/TR/png/#4Concepts.FormatChunks

            In this instance we need to check to see if the starting chunk is a header chunk.
            If it isn't we want to immediately abort any attempts at reading the image.
         */

        while (inputBuffer.hasRemaining()) {
            var chunkLength = inputBuffer.getInt();
            var chunkName = new byte[4];
            var chunkData = new byte[chunkLength];

            inputBuffer.get(chunkName);
            inputBuffer.get(chunkData);

            @SuppressWarnings("unused")
            var chunkCRC = inputBuffer.getInt();

            // Check to see if this is a valid IHDR

        }

        return null;
    }

    /**
     * Parse chunks from a given class type so long as it's a record and can be serialized or deserialized as needed.
     * If a chunk fails to parse a reference to the `EMPTY` chunk instance will be returned in kind.
     * @param chunkType the chunk class to parse to.
     * @param channel the incoming byte channel to parse the contents of.
     * @return a chunk instance of the input chunk class if successful. If unsuccessful for whatever reasong empty
     * is returned.
     * @param <T> The type of the resulting chunk class
     */
    public <T extends Record & Chunk> T parseChunk(Class<T> chunkType, ReadableByteChannel channel) {
        // Make sure the byte channel isn't null
        Objects.requireNonNull(chunkType, "Chunk type is not specified for chunk parsing.");
        Objects.requireNonNull(channel, "No valid ReadableByteChannel has been passed.");

        return null;
    }

}
