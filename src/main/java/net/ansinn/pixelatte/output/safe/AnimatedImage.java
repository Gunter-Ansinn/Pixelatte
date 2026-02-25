package net.ansinn.pixelatte.output.safe;

import java.time.Duration;

public sealed interface AnimatedImage<T extends StaticImage> extends PixelResource permits AnimatedImage8, AnimatedImage16 {

    // The normal image written by the
    T thumbnail();

    int frameCount();
    int loopCount();

    default boolean isInfinite() {
        return  (loopCount() == 0);
    }

    Duration totalDuration();

    Frame<T> getFrameAt(Duration duration);
    Frame<T> getFrame(int index);

}
