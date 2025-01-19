package net.ansinn.pixelatte.formats.png.parsers;

import net.ansinn.pixelatte.IntermediaryImage;
import net.ansinn.pixelatte.formats.png.FilterResult;
import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.util.List;

public interface PNGTypeParser {

    IntermediaryImage parseImage(FilterResult imageData, IHDR header, List<Chunk> chunks);

}
