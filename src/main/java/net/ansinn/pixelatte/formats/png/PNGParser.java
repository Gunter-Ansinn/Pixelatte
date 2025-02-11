package net.ansinn.pixelatte.formats.png;

import net.ansinn.pixelatte.IntermediaryImage;
import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
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
        inputBuffer.get(new byte[8]); // we simply skip ahead eight bytes regardless of where its being read from.

        if (!inputBuffer.hasRemaining())
            throw new IllegalStateException("Malformed PNG, no more data within buffer.");

        /*
            We can make conservative assumptions on needed buffer space for chunk type plus length.
            Max Length is 4 bytes like IHDR each character represented by one byte
            See the spec here: https://www.w3.org/TR/png/#4Concepts.FormatChunks

            In this instance we need to check to see if the starting chunk is a header chunk.
            If it isn't we want to immediately abort any attempts at reading the image.
         */

        //TODO implement simple record decoder library
        var header = parseHeader(inputBuffer);

        while (inputBuffer.hasRemaining()) {
            var chunkLength = inputBuffer.getInt();
            var chunkName = new byte[4];
            var chunkData = new byte[chunkLength];

            inputBuffer.get(chunkName);
            inputBuffer.get(chunkData);

            @SuppressWarnings("unused")
            var chunkCRC = inputBuffer.getInt();

            // Check to see if this is a valid IHDR
            System.out.println("Name: " + new String(chunkName));
            System.out.println("Data Length: " + chunkLength);
            System.out.println("Data: " + Arrays.toString(chunkData));
        }

        return null;
    }

    /**
     * Dedicated function to parse PNG header chunk rather than go through a dispatch via the existing chunk decoding method.
     * Reads an image buffer and returns a header chunk record.
     *
     * @param inputBuffer incoming image information
     * @return header chunk
     */
    private static IHDR parseHeader(ByteBuffer inputBuffer) {
        // Bounds checking for safety. It's best not to cause errors where possible.
        if (inputBuffer.remaining() < 25)
            throw new IllegalStateException("Image has no space for a header chunk.");

        var chunkLength = inputBuffer.getInt();

        // We have assurances on what this chunk should be so lets be safe
        if (chunkLength != 13)
            throw new IllegalStateException("Image header doesn't have proper data length.");

        // Create byte arrays to load in the name and data of the header chunk
        var chunkName = new byte[4];
        var chunkData = new byte[chunkLength];

        inputBuffer.get(chunkName);
        inputBuffer.get(chunkData);

        // Wrap up our inner data within a nice bytebuffer
        var dataBuffer = ByteBuffer.wrap(chunkData);

        @SuppressWarnings("unused")
        var chunkCRC = inputBuffer.getInt();

        var width = dataBuffer.getInt();                            // Image width
        var height = dataBuffer.getInt();                           // Image height
        var bitDepth = dataBuffer.get();                            // Bit depth of image
        var colorType = Chunk.ColorType.values()[dataBuffer.get()]; // Color type enum
        var compressionMethod = dataBuffer.get();                   // compression method of image
        var filterMethod = dataBuffer.get();                         // filter method of image
        var interlacedMethod = dataBuffer.get();                    // whether the image uses interlacing or not

        return new IHDR(width,height,bitDepth, colorType, compressionMethod, filterMethod, interlacedMethod);
    }

}
