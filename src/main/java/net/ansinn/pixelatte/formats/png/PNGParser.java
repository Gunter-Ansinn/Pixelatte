package net.ansinn.pixelatte.formats.png;

import net.ansinn.ByteBarista.SimpleRecordDecoder;
import net.ansinn.pixelatte.IntermediaryImage;
import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.logging.Filter;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public final class PNGParser {

    private static final int IHDR_TAG = ChunkRegistry.toTag("IHDR");
    private static final int IDAT_TAG = ChunkRegistry.toTag("IDAT");
    private static final int IEND_TAG = ChunkRegistry.toTag("IEND");


    private static final Logger logger = Logger.getLogger(PNGParser.class.getName());

    private PNGParser() {}

    /**
     * Parses a PNG from an already proven inputted bytebuffer
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
        try {
            var headerLen = inputBuffer.getInt();
            var headerTag = inputBuffer.getInt();

            if (headerTag != IHDR_TAG)
                throw new IllegalStateException("Image doesn't start with a header chunk.");

            var headerData = new byte[headerLen];

            inputBuffer.get(headerData);

            var headerChunk = (IHDR) ChunkRegistry.decodeChunk(headerTag, headerData, inputBuffer.getInt());
            System.out.println("headerChunk = " + headerChunk);

            var chunks = new ArrayList<Chunk>(5); // Store generic chunks
            var data = new ByteArrayOutputStream(); // Store IDAT chunk data

            while (inputBuffer.hasRemaining()) {
                var chunkLength = inputBuffer.getInt();
                var chunkTag = inputBuffer.getInt();
                var chunkData = new byte[chunkLength];

                inputBuffer.get(chunkData);

                @SuppressWarnings("unused")
                var chunkCRC = inputBuffer.getInt();

                if (chunkTag == IDAT_TAG)
                    data.writeBytes(chunkData);
                else if (ChunkRegistry.isRegistered(chunkTag))
                    chunks.add(ChunkRegistry.decodeChunk(chunkTag, chunkData, chunkCRC));
                else if (chunkTag == IEND_TAG)
                    break;

            }

            Inflater inflater = new Inflater();
            inflater.setInput(data.toByteArray());

            var channels = headerChunk.colorType().getChannels();

            var bitsPerPixel = channels * headerChunk.bitDepth();
            var bitsPerRow = headerChunk.width() * bitsPerPixel;
            var bytesPerRow = (bitsPerRow + 7) / 8;
            var scanlineSize = 1 + bytesPerRow;

            var decompressedSize = headerChunk.height() * scanlineSize;
            
            var output = new byte[decompressedSize];
            inflater.inflate(output);
            System.out.println("output = " + Arrays.toString(output));
            System.out.println("output.length = " + output.length);

            var filteredResult = PNGFilter.process(output, headerChunk);
            System.out.println("filteredResult = " + Arrays.toString(filteredResult));
            System.out.println("filteredResult.length = " + filteredResult.length);

            int width = headerChunk.width();
            int height = headerChunk.height();

            int rowBits = headerChunk.width() * bitsPerPixel;
            int rowBytes = headerChunk.getScanlineByteLength();

            for (int y = 0; y < height; y++) {
                int rowOffset = y * rowBytes;

                for (int x = 0; x < width; x++) {
                    int byteIndex = rowOffset + (x / 8);
                    int bitIndex = 7 - (x % 8); // PNG stores MSB first

                    int bit = (filteredResult[byteIndex] >> bitIndex) & 1;
                    System.out.print(bit == 1 ? "█" : "░");
                }

                System.out.print("\n");
            }

        } catch (IllegalAccessException | NoSuchMethodException | DataFormatException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
