package net.ansinn.pixelatte;

import net.ansinn.pixelatte.formats.png.PNGFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestImageFilter {

    @Test
    void filterNone_shouldReturnOriginal() {
        byte[] original = new byte[]{10, 20, 30};
        byte[] result = original; // None filter leaves data unchanged
        assertArrayEquals(original, result);
    }

    @Test
    void filterSub_shouldApplyCorrectly() {
        byte[] input = new byte[] {10, 20, 30, 40, 50};
        byte[] expected = new byte[] {10, 30, 60, 100, (byte) 150}; // each = raw + left
        byte[] result = PNGFilter.filterSub(input, 1);
        assertArrayEquals(expected, result);
    }

    @Test
    void filterUp_shouldApplyCorrectly() {
        byte[] raw = new byte[] {5, 10, 15};
        byte[] previous = new byte[] {1, 2, 3};
        byte[] expected = new byte[] {6, 12, 18};
        byte[] result = PNGFilter.filterUp(raw, previous);
        assertArrayEquals(expected, result);
    }

    @Test
    void filterAverageSIMD_shouldApplyCorrectly() {
        byte[] raw = new byte[] {10, 20, 30, 40};
        byte[] previous = new byte[] {4, 6, 8, 10};
        byte[] expected = new byte[4];

        // bpp = 1
        expected[0] = (byte) (raw[0] + (previous[0] >>> 1));
        expected[1] = (byte) (raw[1] + ((expected[0] & 0xFF) + (previous[1] & 0xFF)) / 2);
        expected[2] = (byte) (raw[2] + ((expected[1] & 0xFF) + (previous[2] & 0xFF)) / 2);
        expected[3] = (byte) (raw[3] + ((expected[2] & 0xFF) + (previous[3] & 0xFF)) / 2);

        byte[] result = PNGFilter.filterAverageSIMD(raw, previous, 1);
        assertArrayEquals(expected, result);
    }

    @Test
    void filterPaethScalar_shouldApplyCorrectly() {
        byte[] raw = new byte[] {5, 5, 5, 5};
        byte[] previous = new byte[] {3, 6, 9, 12};
        byte[] expected = new byte[4];

        // bpp = 1
        expected[0] = (byte) (raw[0] + PNGFilter.paethPredictor(0, previous[0] & 0xFF, 0));
        expected[1] = (byte) (raw[1] + PNGFilter.paethPredictor(expected[0] & 0xFF, previous[1] & 0xFF, previous[0] & 0xFF));
        expected[2] = (byte) (raw[2] + PNGFilter.paethPredictor(expected[1] & 0xFF, previous[2] & 0xFF, previous[1] & 0xFF));
        expected[3] = (byte) (raw[3] + PNGFilter.paethPredictor(expected[2] & 0xFF, previous[3] & 0xFF, previous[2] & 0xFF));

        byte[] result = PNGFilter.filterPaethScalar(raw, previous, 1);
        assertArrayEquals(expected, result);
    }

}
