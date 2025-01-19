package net.ansinn.pixelatte.formats.png.layout.chunks;


import net.ansinn.pixelatte.formats.png.layout.Chunk;

import java.nio.ByteBuffer;

public record gAMA(float gamma) implements Chunk {

    @Override
    public String toString() {
        return "gAMA{" +
                "gamma=" + gamma +
                '}';
    }

    public static Chunk provider(byte[] data, IHDR header) {
        var buf = ByteBuffer.wrap(data);
        var intensity = buf.getInt();
        var uIntensity = Integer.toUnsignedLong(intensity);

        var gamaIntensity = (float) (uIntensity / 100000);

        return new gAMA(gamaIntensity);
    }
}
