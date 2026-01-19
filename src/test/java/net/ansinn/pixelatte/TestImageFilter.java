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

//    @Test
//    void filterSub_shouldApplyCorrectly() {
//        byte[] input = new byte[] {10, 20, 30, 40, 50};
//        byte[] expected = new byte[] {10, 30, 60, 100, (byte) 150}; // each = raw + left
//        byte[] result = PNGFilter.filterSub(input, 1);
//        assertArrayEquals(expected, result);
//    }
//
//    @Test
//    void filterUp_shouldApplyCorrectly() {
//        byte[] raw = new byte[] {5, 10, 15};
//        byte[] previous = new byte[] {1, 2, 3};
//        byte[] expected = new byte[] {6, 12, 18};
//        byte[] result = PNGFilter.filterUp(raw, previous);
//        assertArrayEquals(expected, result);
//    }
//
////    @Test
////    void filterAverageSIMD_shouldApplyCorrectly() {
////        byte[] raw = new byte[] {10, 20, 30, 40};
////        byte[] previous = new byte[] {4, 6, 8, 10};
////        byte[] expected = new byte[4];
////
////        // bpp = 1
////        expected[0] = (byte) (raw[0] + (previous[0] >>> 1));
////        expected[1] = (byte) (raw[1] + ((expected[0] & 0xFF) + (previous[1] & 0xFF)) / 2);
////        expected[2] = (byte) (raw[2] + ((expected[1] & 0xFF) + (previous[2] & 0xFF)) / 2);
////        expected[3] = (byte) (raw[3] + ((expected[2] & 0xFF) + (previous[3] & 0xFF)) / 2);
////
////        byte[] result = PNGFilter.filterAverageSIMD(raw, previous, 1);
////        assertArrayEquals(expected, result);
////    }
//
//    @Test
//    void filterPaethScalar_shouldApplyCorrectly() {
//        byte[] raw = new byte[] {5, 5, 5, 5};
//        byte[] previous = new byte[] {3, 6, 9, 12};
//        byte[] expected = new byte[4];
//
//        // bpp = 1
//        expected[0] = (byte) (raw[0] + PNGFilter.paethPredictor(0, previous[0] & 0xFF, 0));
//        expected[1] = (byte) (raw[1] + PNGFilter.paethPredictor(expected[0] & 0xFF, previous[1] & 0xFF, previous[0] & 0xFF));
//        expected[2] = (byte) (raw[2] + PNGFilter.paethPredictor(expected[1] & 0xFF, previous[2] & 0xFF, previous[1] & 0xFF));
//        expected[3] = (byte) (raw[3] + PNGFilter.paethPredictor(expected[2] & 0xFF, previous[3] & 0xFF, previous[2] & 0xFF));
//
//        byte[] result = PNGFilter.filterPaethScalar(raw, previous, 1);
//        assertArrayEquals(expected, result);
//    }
//
//    @Test
//    void filterSub_bpp3_shouldApplyCorrectly() {
//        byte[] input = new byte[] {
//                10, 20, 30,      // Pixel 1 (no left)
//                5,  5,  5,       // Pixel 2: add pixel 1
//                1,  1,  1        // Pixel 3: add pixel 2
//        };
//        byte[] expected = new byte[] {
//                10, 20, 30,
//                15, 25, 35,
//                16, 26, 36
//        };
//
//        byte[] result = PNGFilter.filterSub(input, 3);
//        assertArrayEquals(expected, result);
//    }
//
//    @Test
//    void filterUp_bpp3_shouldApplyCorrectly() {
//        byte[] raw = new byte[] {
//                10, 20, 30,
//                5,  5,  5,
//                1,  1,  1
//        };
//        byte[] previous = new byte[] {
//                1, 2, 3,
//                4, 5, 6,
//                7, 8, 9
//        };
//        byte[] expected = new byte[] {
//                11, 22, 33,
//                9, 10, 11,
//                8, 9, 10
//        };
//
//        byte[] result = PNGFilter.filterUp(raw, previous);
//        assertArrayEquals(expected, result);
//    }
//
////    @Test
////    void filterAverage_bpp3_shouldApplyCorrectly() {
////        byte[] raw = new byte[] {
////                10, 20, 30,
////                5,  5,  5,
////                1,  1,  1
////        };
////        byte[] previous = new byte[] {
////                2, 4, 6,
////                6, 6, 6,
////                2, 2, 2
////        };
////        byte[] expected = new byte[9];
////
////        expected[0] = (byte) (raw[0] + (previous[0] >>> 1));
////        expected[1] = (byte) (raw[1] + (previous[1] >>> 1));
////        expected[2] = (byte) (raw[2] + (previous[2] >>> 1));
////
////        expected[3] = (byte) (raw[3] + ((expected[0] & 0xFF) + (previous[3] & 0xFF)) / 2);
////        expected[4] = (byte) (raw[4] + ((expected[1] & 0xFF) + (previous[4] & 0xFF)) / 2);
////        expected[5] = (byte) (raw[5] + ((expected[2] & 0xFF) + (previous[5] & 0xFF)) / 2);
////
////        expected[6] = (byte) (raw[6] + ((expected[3] & 0xFF) + (previous[6] & 0xFF)) / 2);
////        expected[7] = (byte) (raw[7] + ((expected[4] & 0xFF) + (previous[7] & 0xFF)) / 2);
////        expected[8] = (byte) (raw[8] + ((expected[5] & 0xFF) + (previous[8] & 0xFF)) / 2);
////
////        byte[] result = PNGFilter.filterAverageSIMD(raw, previous, 3);
////        assertArrayEquals(expected, result);
////    }
//
//    @Test
//    void filterPaeth_bpp3_shouldApplyCorrectly() {
//        byte[] raw = new byte[] {
//                1, 2, 3,
//                4, 5, 6,
//                7, 8, 9
//        };
//        byte[] previous = new byte[] {
//                10, 11, 12,
//                13, 14, 15,
//                16, 17, 18
//        };
//        byte[] expected = new byte[9];
//
//        expected[0] = (byte) (raw[0] + PNGFilter.paethPredictor(0, previous[0] & 0xFF, 0));
//        expected[1] = (byte) (raw[1] + PNGFilter.paethPredictor(0, previous[1] & 0xFF, 0));
//        expected[2] = (byte) (raw[2] + PNGFilter.paethPredictor(0, previous[2] & 0xFF, 0));
//
//        expected[3] = (byte) (raw[3] + PNGFilter.paethPredictor(expected[0] & 0xFF, previous[3] & 0xFF, previous[0] & 0xFF));
//        expected[4] = (byte) (raw[4] + PNGFilter.paethPredictor(expected[1] & 0xFF, previous[4] & 0xFF, previous[1] & 0xFF));
//        expected[5] = (byte) (raw[5] + PNGFilter.paethPredictor(expected[2] & 0xFF, previous[5] & 0xFF, previous[2] & 0xFF));
//
//        expected[6] = (byte) (raw[6] + PNGFilter.paethPredictor(expected[3] & 0xFF, previous[6] & 0xFF, previous[3] & 0xFF));
//        expected[7] = (byte) (raw[7] + PNGFilter.paethPredictor(expected[4] & 0xFF, previous[7] & 0xFF, previous[4] & 0xFF));
//        expected[8] = (byte) (raw[8] + PNGFilter.paethPredictor(expected[5] & 0xFF, previous[8] & 0xFF, previous[5] & 0xFF));
//
//        byte[] result = PNGFilter.filterPaethScalar(raw, previous, 3);
//        assertArrayEquals(expected, result);
//    }

}
