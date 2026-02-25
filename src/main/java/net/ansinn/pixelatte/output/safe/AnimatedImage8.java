package net.ansinn.pixelatte.output.safe;

import net.ansinn.pixelatte.formats.png.layout.ChunkMap;

import java.time.Duration;

public record AnimatedImage8(
        int frameCount,
        int loopCount,
        int width,
        int height,
        Format format,
        ChunkMap chunkMap,
        StaticImage8 thumbnail
) implements AnimatedImage<StaticImage8>, BitDepth.Bit8 {

    @Override
    public AnimatedImage8 copy() {
        // Ensure the thumbnail is also deep-copied.
        StaticImage8 thumbnailCopy = this.thumbnail.copy();
        return new AnimatedImage8(this.frameCount, this.loopCount, this.width, this.height, this.format, this.chunkMap, thumbnailCopy);
    }

    @Override
    public byte[] data() {
        return thumbnail().data();
    }

    @Override
    public Duration totalDuration() {
        return null;
    }

    @Override
    public Frame<StaticImage8> getFrameAt(Duration duration) {
        return null;
    }

    @Override
    public Frame<StaticImage8> getFrame(int index) {
        return null;
    }

}
