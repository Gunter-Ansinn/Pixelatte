package net.ansinn.pixelatte.output.safe;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import net.ansinn.pixelatte.formats.png.layout.Chunk;
import net.ansinn.pixelatte.formats.png.layout.chunks.IHDR;

import java.util.Arrays;

public sealed interface BitDepth {

    byte getDepth();

    void invert();

    void clear();

    void threshold(int level);

//    sealed interface Bit1 extends BitDepth {
//
//        @Override
//        default byte getDepth() {
//            return 1;
//        }
//    }
//
//    sealed interface Bit2 extends BitDepth {
//
//        @Override
//        default byte getDepth() {
//            return 2;
//        }
//    }
//
//    sealed interface Bit4 extends BitDepth {
//
//        @Override
//        default byte getDepth() {
//            return 4;
//        }
//    }

    sealed interface Bit8 extends BitDepth permits AnimatedImage8, GrayScale8, StaticImage8 {

        byte[] data();

        /**
         * Fills the entire data array with the specified byte value.
         * This method is vectorized for performance.
         * @param value the byte value to fill the array with.
         */
        default void fill(byte value) {
            var SPECIES = ByteVector.SPECIES_PREFERRED;
            int i = 0;
            var fillVec = ByteVector.broadcast(SPECIES, value);
            for (; i < SPECIES.loopBound(data().length); i += SPECIES.length()) {
                fillVec.intoArray(data(), i);
            }
            for (; i < data().length; i++) {
                data()[i] = value;
            }
        }

        default void invert() {
            var SPECIES = ByteVector.SPECIES_PREFERRED;
            
            int i = 0;
            for (; i < SPECIES.loopBound(data().length); i += SPECIES.length()) {
                var v = ByteVector.fromArray(SPECIES, data(), i);
                v.not().intoArray(data(), i);
            }
            for (; i < data().length; i++) {
                data()[i] = (byte) ~data()[i];
            }
        }

        @Override
        default void clear() {
            Arrays.fill(data(), (byte) 0);
        }

        @Override
        default void threshold(int level) {
            var SPECIES = ByteVector.SPECIES_PREFERRED;
            int i = 0;
            int limit = SPECIES.loopBound(data().length);

            // To perform an unsigned comparison on signed bytes, we can XOR both values with 0x80.
            // This shifts the range [0, 255] to [-128, 127], which can be correctly compared.
            byte adjustedLevel = (byte) (level ^ 0x80);
            var levelVec = ByteVector.broadcast(SPECIES, adjustedLevel);
            var xor_mask = ByteVector.broadcast(SPECIES, (byte) 0x80);
            var full_white = ByteVector.broadcast(SPECIES, (byte) 0xFF);
            var full_black = ByteVector.zero(SPECIES);

            for (; i < limit; i += SPECIES.length()) {
                var vec = ByteVector.fromArray(SPECIES, data(), i);
                // Apply the XOR mask to shift to a comparable range
                var adjustedVec = vec.lanewise(VectorOperators.XOR, xor_mask);
                // Now perform the signed comparison
                VectorMask<Byte> mask = adjustedVec.compare(VectorOperators.GT, levelVec);
                // Blend the results: 255 where true, 0 where false
                full_black.blend(full_white, mask).intoArray(data(), i);
            }
            // Handle remaining elements with a correct scalar unsigned comparison
            for (; i < data().length; i++) {
                data()[i] = (data()[i] & 0xFF) > level ? (byte) 0xFF : 0;
            }
        }

        @Override
        default byte getDepth() {
            return 8;
        }
    }

    sealed interface Bit16 extends BitDepth permits StaticImage16, AnimatedImage16 {

        short[] data();

        /**
         * Fills the entire data array with the specified short value.
         * This method is vectorized for performance.
         * @param value the short value to fill the array with.
         */
        default void fill(short value) {
            var SPECIES = ShortVector.SPECIES_PREFERRED;
            int i = 0;
            var fillVec = ShortVector.broadcast(SPECIES, value);
            for (; i < SPECIES.loopBound(data().length); i += SPECIES.length()) {
                fillVec.intoArray(data(), i);
            }
            for (; i < data().length; i++) {
                data()[i] = value;
            }
        }

        @Override
        default void invert() {
            var SPECIES = ShortVector.SPECIES_PREFERRED;
            int i = 0;
            for (; i < SPECIES.loopBound(data().length); i += SPECIES.length()) {
                var v = ShortVector.fromArray(SPECIES, data(), i);
                v.not().intoArray(data(), i);
            }
            for (; i < data().length; i++) {
                data()[i] = (short) ~data()[i];
            }
        }

        @Override
        default void clear() {
            Arrays.fill(data(), (short) 0);
        }

        @Override
        default void threshold(int level) {
            var SPECIES = ShortVector.SPECIES_PREFERRED;
            int i = 0;
            int limit = SPECIES.loopBound(data().length);

            // Same principle as Bit8: XOR with 0x8000 to allow for correct signed comparison
            short adjustedLevel = (short) (level ^ 0x8000);
            var levelVec = ShortVector.broadcast(SPECIES, adjustedLevel);
            var xor_mask = ShortVector.broadcast(SPECIES, (short) 0x8000);
            var full_white = ShortVector.broadcast(SPECIES, (short) 0xFFFF);
            var full_black = ShortVector.zero(SPECIES);

            for (; i < limit; i += SPECIES.length()) {
                var vec = ShortVector.fromArray(SPECIES, data(), i);
                var adjustedVec = vec.lanewise(VectorOperators.XOR, xor_mask);
                VectorMask<Short> mask = adjustedVec.compare(VectorOperators.GT, levelVec);
                full_black.blend(full_white, mask).intoArray(data(), i);
            }
            // Handle remaining elements
            for (; i < data().length; i++) {
                data()[i] = (data()[i] & 0xFFFF) > level ? (short) 0xFFFF : 0;
            }
        }

        @Override
        default byte getDepth() {
            return 16;
        }
    }
}
