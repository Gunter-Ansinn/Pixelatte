package net.ansinn.pixelatte.output.safe;

import net.ansinn.pixelatte.output.ImageMeta;

public sealed interface Grayscale extends ImageMeta permits GrayScale8 {

    float getLuminance(int x, int y);

}
