package net.ansinn.pixelatte.formats.png;

import net.ansinn.ByteBarista.SimpleRecordDecoder;
import net.ansinn.pixelatte.IntermediaryImage;
import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.*;
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

        try {
            var headerLen = inputBuffer.getInt();
            var headerTag = inputBuffer.getInt();

            if (headerTag != IHDR_TAG)
                throw new IllegalStateException("Image doesn't start with a header chunk.");

            var headerData = new byte[headerLen];

            inputBuffer.get(headerData);

            var headerChunk = SimpleRecordDecoder.decodeRecord(ByteBuffer.wrap(headerData), IHDR.class);
            var chunks = new ChunkMap(); // Store generic chunks
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
                    chunks.addChunk(ChunkRegistry.decodeChunk(chunkTag, chunkData, chunkCRC, headerChunk));
                else if (chunkTag == IEND_TAG)
                    break;

            }

            var output = inflateBuffer(data.toByteArray(), headerChunk);
            var filteredResult = PNGFilter.process(output, headerChunk);
            System.out.println("filteredResult = " + Arrays.toString(filteredResult));
            System.out.println("filteredResult.length = " + filteredResult.length);
            var unpacked = PNGUnpacker.unpack(filteredResult, headerChunk, chunks);

        } catch (IllegalAccessException | NoSuchMethodException | DataFormatException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static byte[] inflateBuffer(byte[] data, IHDR headerChunk) throws DataFormatException {
        // Create the inflater for the compressed data
        Inflater inflater = new Inflater();

        // Set the target data
        inflater.setInput(data);

        // Calculate the output size and then call inflation code
        var output = new byte[calculateDecompressedSize(headerChunk)];
        inflater.inflate(output);

        return output;
    }

    private static int calculateDecompressedSize(IHDR headerChunk) {
        var channels = headerChunk.colorType().getChannels();
        var bitsPerPixel = channels * headerChunk.bitDepth();
        var bitsPerRow = headerChunk.width() * bitsPerPixel;
        var bytesPerRow = (bitsPerRow + 7) / 8;
        var scanlineSize = 1 + bytesPerRow;
        return headerChunk.height() * scanlineSize;
    }

}
