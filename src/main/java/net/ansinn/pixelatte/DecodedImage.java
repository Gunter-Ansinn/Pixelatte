package net.ansinn.pixelatte;

import net.ansinn.pixelatte.formats.png.layout.ChunkMap;

public sealed interface DecodedImage permits DecodedImage8, DecodedImage16 {

    int width();
    int height();
    Format format();
    ChunkMap chunkMap();

    enum Format {
        RGBA8(4), RGB8(3), GRAY8(1),
        RGBA16(4), RGB16(3), GRAY16(1);

        private final int channels;

        Format(int channels) {
            this.channels = channels;
        }

        public int channels() {
            return channels;
        }
    }

}
