package net.ansinn.pixelatte.formats.png;

import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestImageFilter {

    private IHDR createHeader(int width, int height, int bitDepth, Chunk.ColorType colorType) {
        return new IHDR(width, height, (byte) bitDepth, colorType, (byte) 0, (byte) 0, (byte) 0);
    }

    @Test
    void process_filterNone_shouldReturnOriginal() {
        // 1 pixel, 3 bytes (TrueColor 8bit)
        // Row: [Filter(0), R, G, B]
        byte[] input = new byte[]{0, 10, 20, 30};
        IHDR header = createHeader(1, 1, 8, Chunk.ColorType.TrueColor);

        byte[] expected = new byte[]{10, 20, 30};
        byte[] result = PNGFilter.process(input, header);

        assertArrayEquals(expected, result);
    }

    @Test
    void process_filterSub_shouldApplyCorrectly() {
        // 2 pixels, 3 bytes/pixel (TrueColor 8bit)
        // Row: [Filter(1), P1_R, P1_G, P1_B, P2_R, P2_G, P2_B]
        // P1 has no left, so raw P1 = result P1
        // P2 has left P1, so result P2 = raw P2 + result P1
        // Raw: P1(10, 20, 30), P2(1, 1, 1)
        // Expected: P1(10, 20, 30), P2(11, 21, 31)
        byte[] input = new byte[]{1, 10, 20, 30, 1, 1, 1};
        IHDR header = createHeader(2, 1, 8, Chunk.ColorType.TrueColor);

        byte[] expected = new byte[]{10, 20, 30, 11, 21, 31};
        byte[] result = PNGFilter.process(input, header);

        assertArrayEquals(expected, result);
    }

    @Test
    void process_filterUp_shouldApplyCorrectly() {
        // 1 pixel wide, 2 rows high. TrueColor 8bit (3 bytes/pixel).
        // Row 1: Filter(0) None. [0, 10, 20, 30] -> Res [10, 20, 30]
        // Row 2: Filter(2) Up.   [2, 1, 2, 3]
        //        Result = Raw + Above
        //        Res = [1+10, 2+20, 3+30] = [11, 22, 33]
        
        byte[] input = new byte[]{
                0, 10, 20, 30,  // Row 1
                2, 1, 2, 3      // Row 2
        };
        IHDR header = createHeader(1, 2, 8, Chunk.ColorType.TrueColor);

        byte[] expected = new byte[]{
                10, 20, 30,
                11, 22, 33
        };
        byte[] result = PNGFilter.process(input, header);

        assertArrayEquals(expected, result);
    }

    @Test
    void process_filterAverage_shouldApplyCorrectly() {
        // 1 pixel wide, 2 rows high. TrueColor 8bit (3 bytes/pixel).
        // Row 1: Filter(0) None. [0, 10, 20, 30] -> Res [10, 20, 30]
        // Row 2: Filter(3) Average. [3, 2, 4, 6]
        //        Result = Raw + floor((Left + Above) / 2)
        //        Here Left is 0 (start of row).
        //        Byte 0: Raw 2. Above 10. Avg(0, 10) = 5. Res = 2+5=7
        //        Byte 1: Raw 4. Above 20. Avg(0, 20) = 10. Res = 4+10=14
        //        Byte 2: Raw 6. Above 30. Avg(0, 30) = 15. Res = 6+15=21
        
        byte[] input = new byte[]{
                0, 10, 20, 30,
                3, 2, 4, 6
        };
        IHDR header = createHeader(1, 2, 8, Chunk.ColorType.TrueColor);

        byte[] expected = new byte[]{
                10, 20, 30,
                7, 14, 21
        };
        byte[] result = PNGFilter.process(input, header);

        assertArrayEquals(expected, result);
    }

    @Test
    void process_filterPaeth_shouldApplyCorrectly() {
        // 1 pixel wide, 2 rows high. TrueColor 8bit (3 bytes/pixel).
        // Row 1: Filter(0) None. [0, 10, 20, 30] -> Res [10, 20, 30]
        // Row 2: Filter(4) Paeth. [4, 1, 1, 1]
        //        Left = 0. UpperLeft = 0. Above = (10, 20, 30)
        //        Paeth(a=Left, b=Above, c=UpperLeft)
        //        Byte 0: Paeth(0, 10, 0). p=10. pa=10, pb=0, pc=10. Smallest pb -> Pred=Above=10. Res=1+10=11.
        //        Byte 1: Paeth(0, 20, 0). p=20. pb=0. Pred=Above=20. Res=1+20=21.
        //        Byte 2: Paeth(0, 30, 0). p=30. pb=0. Pred=Above=30. Res=1+30=31.
        
        byte[] input = new byte[]{
                0, 10, 20, 30,
                4, 1, 1, 1
        };
        IHDR header = createHeader(1, 2, 8, Chunk.ColorType.TrueColor);

        byte[] expected = new byte[]{
                10, 20, 30,
                11, 21, 31
        };
        byte[] result = PNGFilter.process(input, header);

        assertArrayEquals(expected, result);
    }
    
    @Test
    void process_mixedFilters() {
        // 2 pixels wide, 2 rows high. 1 byte/pixel (Grayscale 8bit).
        // Row 1: Sub(1). [1, 10, 5]
        //        P1: Raw 10. Left 0. Res=10.
        //        P2: Raw 5. Left 10. Res=15.
        //        Row 1 Res: [10, 15]
        // Row 2: Up(2). [2, 1, 2]
        //        P1: Raw 1. Above 10. Res=11.
        //        P2: Raw 2. Above 15. Res=17.
        //        Row 2 Res: [11, 17]
        
        byte[] input = new byte[]{
                1, 10, 5,
                2, 1, 2
        };
        IHDR header = createHeader(2, 2, 8, Chunk.ColorType.Grayscale);
        
        byte[] expected = new byte[]{
                10, 15,
                11, 17
        };
        byte[] result = PNGFilter.process(input, header);
        
        assertArrayEquals(expected, result);
    }
}