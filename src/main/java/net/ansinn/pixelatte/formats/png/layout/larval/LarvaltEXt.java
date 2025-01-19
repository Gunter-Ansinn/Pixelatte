package net.ansinn.pixelatte.formats.png.layout.larval;


import net.ansinn.pixelatte.formats.png.layout.Chunk;

public record LarvaltEXt() implements Chunk {


    @Override
    public boolean isLarval() {
        return true;
    }
}
