package net.ansinn.pixelatte.editing;

@FunctionalInterface
public interface PixelKernel {

    /**
     * @param x, y The normalized coordinates (0.0 to 1.0)
     * @param r, g, b, a The normalized channel values (0.0 to 1.0)
     * @return The new normalized channel values [r, g, b, a]
     */
   float[] sample(float x, float y, float r, float g, float b, float a);

}
