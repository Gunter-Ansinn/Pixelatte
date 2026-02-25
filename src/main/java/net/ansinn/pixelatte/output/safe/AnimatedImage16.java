package net.ansinn.pixelatte.output.safe;

import net.ansinn.pixelatte.formats.png.layout.ChunkMap;

import java.time.Duration;

public record AnimatedImage16(
        int frameCount,
        int loopCount,
        int width,
        int height,
        Format format,
        ChunkMap chunkMap,
        StaticImage16 thumbnail
) implements AnimatedImage<StaticImage16>, BitDepth.Bit16 {

    @Override
    public AnimatedImage16 copy() {
        // Ensure the thumbnail is also deep-copied.
        StaticImage16 thumbnailCopy = this.thumbnail.copy();
        return new AnimatedImage16(this.frameCount, this.loopCount, this.width, this.height, this.format, this.chunkMap, thumbnailCopy);
    }

    @Override
    public short[] data() {
        return thumbnail().data();
    }

    @Override
    public Duration totalDuration() {
        return null;
    }

    @Override
    public Frame<StaticImage16> getFrameAt(Duration duration) {
        return null;
    }

    @Override
    public Frame<StaticImage16> getFrame(int index) {
        return null;
    }
}
