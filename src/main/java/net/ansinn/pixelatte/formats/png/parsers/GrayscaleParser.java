package net.ansinn.pixelatte.formats.png.parsers;


import net.ansinn.pixelatte.IntermediaryImage;
import net.ansinn.pixelatte.formats.png.FilterResult;
import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.util.List;

public final class GrayscaleParser implements PNGTypeParser {

    public static final GrayscaleParser INSTANCE = new GrayscaleParser();

    private GrayscaleParser() {}

    @Override
    public IntermediaryImage parseImage(FilterResult imageData, IHDR header, List<Chunk> chunks) {
        return switch (imageData) {
            case FilterResult.ByteResult result -> parseByteResult(result.result(), header, chunks);
            default -> throw new IllegalStateException("Boobar ");
        };
    }

    public IntermediaryImage parseByteResult(byte[] bytes, IHDR header, List<Chunk> chunks) {
        return null;
    }


}
