package net.ansinn.pixelatte.formats.png.parsers;

import net.ansinn.pixelatte.IntermediaryImage;
import net.ansinn.pixelatte.formats.png.FilterResult;
import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.util.List;

/**
 * Generic class for handling only permutations of indexed png images.
 */
public final class Indexed implements PNGTypeParser {

    public static final Indexed INSTANCE = new Indexed();

    private Indexed() {}

    @Override
    public IntermediaryImage parseImage(FilterResult imageData, IHDR header, List<Chunk> chunks) {
        return null;
    }
}
