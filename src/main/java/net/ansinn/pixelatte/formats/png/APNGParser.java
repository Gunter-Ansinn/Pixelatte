package net.ansinn.pixelatte.formats.png;

import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;
import net.ansinn.pixelatte.formats.png.layout.chunks.acTL;
import net.ansinn.pixelatte.formats.png.layout.chunks.fcTL;
import net.ansinn.pixelatte.output.safe.*;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static net.ansinn.pixelatte.formats.png.ChunkRegistry.*;
import static net.ansinn.pixelatte.formats.png.PNGParser.calculateDecompressedSize;

public final class APNGParser {

    public static PixelResource parse(ByteBuffer buffer, IHDR header, ChunkMap globalChunks, final ParserResources resources) throws IllegalAccessException, NoSuchMethodException {

        // Parse actl chunk
        var acTL = globalChunks.getFirst(acTL.class).orElseThrow(
                () -> new IllegalStateException("No acTL class exists within animated PNG type"));

        // Loop State
        fcTL currentControl = null;
        IHDR currentFrameHeader = null;
        byte[] currentFiltered = null;
        int currentOffset = 0;

        // Create our list of frames
        List<Frame<StaticImage>> frames = new ArrayList<>();

        while (buffer.hasRemaining()) {
            int len = buffer.getInt();
            int type = buffer.getInt();

            if (type == fcTL_TAG) {
                // Finish Previous Frame
                if (currentControl != null) {
                    frames.add(finishFrame(currentControl, currentFrameHeader, currentFiltered, globalChunks));
                }

                // Parse New Control Chunk
                byte[] data = new byte[len];
                buffer.get(data);
                // We decode fcTL manually here using registry logic or specialized decoder if needed
                // Assuming ChunkRegistry.decodeChunk handles fcTL correctly
                currentControl = (fcTL) ChunkRegistry.decodeChunk(type, data, 0, header);
                buffer.getInt(); // Skip CRC

                // Setup New State
                currentFrameHeader = header.withDimensions((int)currentControl.width(), (int)currentControl.height());
                currentFiltered = new byte[calculateDecompressedSize(currentFrameHeader)];
                currentOffset = 0;
                resources.inflater.reset();

            } else if (type == fdAT_TAG) {
                // Animation Data
                buffer.getInt(); // Skip Sequence Number
                currentOffset = PNGParser.parseIDAT(buffer, len - 4, resources, currentFiltered, currentOffset);

            } else if (type == IDAT_TAG) {
                // Default Image Data (Frame 0 usually)
                if (currentControl != null) {
                    currentOffset = PNGParser.parseIDAT(buffer, len, resources, currentFiltered, currentOffset);
                } else {
                    // Skip IDAT if it's the default image and not part of animation (no fcTL yet)
                    buffer.position(buffer.position() + len + 4);
                }

            } else if (type == IEND_TAG) {
                break;
            } else {
                // Skip unknown chunks
                buffer.position(buffer.position() + len + 4);
            }
        }

        // Finish Final Frame
        if (currentControl != null) {
            frames.add(finishFrame(currentControl, currentFrameHeader, currentFiltered, globalChunks));
        }

        // Return Result
        // We construct an AnimatedImage based on bit depth
        if (header.bitDepth() == 16) {
            return new AnimatedImage16(
                    (int) acTL.frameCount(),
                    (int) acTL.playCount(),
                    header.width(),
                    header.height(),
                    StaticImage.Format.RGBA16, // Assuming RGBA for now, or derive from ColorType
                    globalChunks,
                    (StaticImage16) frames.getFirst().image() // Use first frame as thumbnail for now
            );
        } else {
            return new AnimatedImage8(
                    (int) acTL.frameCount(),
                    (int) acTL.playCount(),
                    header.width(),
                    header.height(),
                    StaticImage.Format.RGBA8, // Assuming RGBA for now
                    globalChunks,
                    (StaticImage8) frames.getFirst().image()
            );
        }
    }

    private static Frame<StaticImage> finishFrame(fcTL control, IHDR header, byte[] filtered, ChunkMap globalChunks) {
        // 1. Unfilter
        byte[] pixels = PNGFilter.process(filtered, header);

        // 2. Unpack
        StaticImage image = PNGUnpacker.unpack(pixels, header, globalChunks);

        // 3. Duration
        long num = control.delayNumerator();
        long den = control.delayDenominator();
        if (den == 0) den = 100; // Spec says 0 means 1/100 sec
        Duration d = Duration.ofMillis((long) ((num * 1000.0) / den));

        // 4. Wrap
        return new Frame<>(image, d, (int)control.xOffset(), (int)control.yOffset(), control.disposeOp(), control.blendOp());
    }
}
