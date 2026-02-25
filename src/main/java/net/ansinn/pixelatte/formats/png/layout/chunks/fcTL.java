package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.ByteBarista.annotations.UnsignedInteger;
import net.ansinn.ByteBarista.annotations.UnsignedShort;
import net.ansinn.pixelatte.formats.png.layout.Chunk;

public record fcTL(
        @UnsignedInteger long sequenceNumber,
        @UnsignedInteger long width,
        @UnsignedInteger long height,
        @UnsignedInteger long xOffset,
        @UnsignedInteger long yOffset,
        @UnsignedShort int delayNumerator,
        @UnsignedShort int delayDenominator,
        DisposeOp disposeOp,
        BlendOp blendOp
) implements Chunk {

    // Descriptions from Spec
    public enum DisposeOp {
        /**
         * no disposal is done on this frame before rendering the next; the contents of the output buffer are left as is.
         */
        APNG_DISPOSE_OP_NONE,

        /**
         * the frame's region of the output buffer is to be cleared to fully transparent black before rendering the next frame.
         */
        APNG_DISPOSE_OP_BACKGROUND,

        /**
         * the frame's region of the output buffer is to be reverted to the previous contents before rendering the next frame.
         */
        APNG_DISPOSE_OP_PREVIOUS
    }

    public enum BlendOp {
        /**
         * all color components of the frame, including alpha, overwrite the current contents of the frame's output buffer region.
         */
        APNG_BLEND_OP_SOURCE,

        /**
         * the frame should be composited onto the output buffer based on its alpha
         */
        APNG_BLEND_OP_OVER
    }
}
