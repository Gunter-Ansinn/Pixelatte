package net.ansinn.pixelatte.output.safe;

import net.ansinn.pixelatte.formats.png.layout.chunks.fcTL;

import java.time.Duration;

public record Frame<T extends StaticImage>(T image, Duration delay, int xOffset, int yOffset, fcTL.DisposeOp disposeOp, fcTL.BlendOp blendOp) {
}
