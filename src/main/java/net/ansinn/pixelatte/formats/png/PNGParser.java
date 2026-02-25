package net.ansinn.pixelatte.formats.png;

import net.ansinn.ByteBarista.SimpleRecordDecoder;
import net.ansinn.pixelatte.formats.png.layout.chunks.acTL;
import net.ansinn.pixelatte.output.safe.PixelResource;
import net.ansinn.pixelatte.output.safe.StaticImage;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static net.ansinn.pixelatte.formats.png.ChunkRegistry.*;

public final class PNGParser {

    private static final int IHDR_BYTE_SIZE = 13;

    private static final Logger logger = Logger.getLogger(PNGParser.class.getName());

    // We want to pre-allocate "heavy hitters" to be a thread local object
    private static final ThreadLocal<ParserResources> ThreadLocalResources = ThreadLocal.withInitial(ParserResources::new);

    private PNGParser() {}

    /**
     * Parses a PNG from an already proven input bytebuffer
     * @param inputBuffer the input buffer data
     * @return
     */
    public static PixelResource parse(ByteBuffer inputBuffer) {
        var resources = ThreadLocalResources.get();

        //Make sure input stream isn't null
        Objects.requireNonNull(inputBuffer, "The input buffer is null");
        inputBuffer.get(resources.headerSkip); // we simply skip ahead eight bytes regardless of where its being read from.

        if (!inputBuffer.hasRemaining())
            throw new IllegalStateException("Malformed PNG, no more data within buffer.");

        try {

            var headerChunk = parseHeader(inputBuffer);
            var chunks = new ChunkMap(); // Store generic chunks

            var inflater = resources.inflater;
            inflater.reset();

            var filteredResult = new byte[calculateDecompressedSize(headerChunk)];
            var decompressionOffset = 0;


            while (inputBuffer.hasRemaining()) {
                var chunkLength = inputBuffer.getInt();
                var chunkTag = inputBuffer.getInt();

                if (chunkTag == IDAT_TAG) {
                    decompressionOffset = parseIDAT(inputBuffer, chunkLength, resources, filteredResult, decompressionOffset);
                    continue;
                }

                // Load our chunk data into memory
                var chunkData = new byte[chunkLength];
                inputBuffer.get(chunkData);

                @SuppressWarnings("unused")
                var chunkCRC = inputBuffer.getInt();

                if (ChunkRegistry.isRegistered(chunkTag))
                    chunks.addChunk(ChunkRegistry.decodeChunk(chunkTag, chunkData, chunkCRC, headerChunk));

                // Abort current parsing and hand everything off to the. APNG parser.
                if (chunkTag == acTL_TAG)
                    return APNGParser.parse(inputBuffer, headerChunk, chunks, resources);

                else if (chunkTag == IEND_TAG)
                    break;

            }

            var finalPixels = PNGFilter.process(filteredResult, headerChunk);

            return PNGUnpacker.unpack(finalPixels, headerChunk, chunks);

        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses IDAT chunks into usable data to be deflated into the bufferOutput array.
     * @param inputBuffer buffer being inflated
     * @param chunkLength length of IDAT chunk
     * @param resources the ParserResources where the inflater and scratch buffer are stored
     * @param bufferOutput where the inflated bytes are written
     * @param offset offset of the inflated pixel data
     * @return current offset
     */
    static int parseIDAT(final ByteBuffer inputBuffer, final int chunkLength, final ParserResources resources, final byte[] bufferOutput, final int offset) {
        var decompressionOffset = offset;
        var inflater = resources.inflater;

        // Prepare a chunk data view to avoid allocation of new byte[] arrays
        var oldLimit = inputBuffer.limit();
        inputBuffer.limit(inputBuffer.position() + chunkLength);

        // Allocate a reusable byte array
        byte[] chunkScratch = resources.scratch;
        var bytesProcessed = 0;

        while (bytesProcessed < chunkLength) {
            var toRead = Math.min(chunkScratch.length, chunkLength - bytesProcessed);
            inputBuffer.get(chunkScratch, 0, toRead);

            inflater.setInput(chunkScratch, 0, toRead);

            try {
                while (!inflater.needsInput()) {
                    var inflatedBytes = inflater.inflate(bufferOutput, decompressionOffset, bufferOutput.length - decompressionOffset);

                    if (inflatedBytes == 0)
                        break;

                    decompressionOffset += inflatedBytes;
                }
            } catch (DataFormatException e) {
                throw new RuntimeException("Corrupt PNG data", e);
            }

            bytesProcessed += toRead;
        }

        // Restore limit
        inputBuffer.limit(oldLimit);
        inputBuffer.getInt(); // skip CRC
        return decompressionOffset;
    }

    private static IHDR parseHeader(ByteBuffer inputBuffer) throws IllegalAccessException, NoSuchMethodException {
        var headerLen = inputBuffer.getInt();

        if (headerLen < IHDR_BYTE_SIZE || headerLen > 100)
            throw new IllegalArgumentException("IHDR length invalid: " + headerLen);

        var headerTag = inputBuffer.getInt();

        if (headerTag != IHDR_TAG)
            throw new IllegalStateException("Image doesn't start with a header chunk.");

        var headerData = new byte[headerLen];

        inputBuffer.get(headerData);

        var headerChunk = SimpleRecordDecoder.decodeRecord(ByteBuffer.wrap(headerData), IHDR.class);

        // Skip CRC value
        inputBuffer.getInt();

        return headerChunk;
    }

    static int calculateDecompressedSize(IHDR headerChunk) {
        var channels = headerChunk.colorType().getChannels();
        var bitsPerPixel = channels * headerChunk.bitDepth();
        var bitsPerRow = headerChunk.width() * bitsPerPixel;
        var bytesPerRow = (bitsPerRow + 7) / 8;
        var scanlineSize = 1 + bytesPerRow;
        return headerChunk.height() * scanlineSize;
    }

}
