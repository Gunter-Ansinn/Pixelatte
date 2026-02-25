package net.ansinn.pixelatte.formats.png;

import net.ansinn.pixelatte.output.safe.StaticImage;
import net.ansinn.pixelatte.formats.png.layout.ChunkMap;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;
import net.ansinn.pixelatte.formats.png.unpackers.*;

public class PNGUnpacker {
    public static StaticImage unpack(final byte[] filtered, final IHDR header, final ChunkMap chunkMap) {
        return switch (header.colorType()) {
            case Grayscale -> GrayscaleUnpacker.unpackGrayscale(filtered, header, chunkMap);
            case TrueColor -> TrueColorUnpacker.unpackTrueColor(filtered, header, chunkMap);
            case Indexed -> IndexedUnpacker.unpackIndexed(filtered, header, chunkMap);
            case GreyscaleAlpha -> GrayscaleAlphaUnpacker.unpackGrayscaleAlpha(filtered, header, chunkMap);
            case TrueColorAlpha -> TrueColorAlphaUnpacker.unpackTrueColorAlpha(filtered, header, chunkMap);
            default -> throw new IllegalStateException("Invalid image color format: " + header.colorType().ordinal());
        };
    }
}
