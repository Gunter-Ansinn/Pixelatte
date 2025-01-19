package net.ansinn.pixelatte.formats.png;

/**
 * A wrapper class for the output numbers.
 */
public sealed interface NibbledOutput {

    Empty EMPTY = new Empty();

    record Bits(byte[] bits) implements NibbledOutput {}

    record BitPairs(byte[] bitPairs) implements NibbledOutput {}

    record Nibbles(byte[] nibbles) implements NibbledOutput {}

    record Bytes(int[] bytes) implements NibbledOutput {}

    record Shorts(int[] shorts) implements NibbledOutput {}

    record Integers(int[] ints) implements NibbledOutput {}

    final class Empty implements NibbledOutput {
        private Empty() {}
    }
}
