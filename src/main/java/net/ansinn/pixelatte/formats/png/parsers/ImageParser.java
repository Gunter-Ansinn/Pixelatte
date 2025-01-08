package net.ansinn.pixelatte.formats.png.parsers;

import net.ansinn.cappuccino.IO.textures.FilterResult;
import net.ansinn.cappuccino.IO.textures.layout.Chunk;
import net.ansinn.cappuccino.IO.textures.layout.chunks.IHDR;
import net.ansinn.cappuccino.rendering.textures.Texture;

import java.util.List;

public interface ImageParser {

    Texture parseImage(FilterResult imageData, IHDR header, List<Chunk> chunks);

}
