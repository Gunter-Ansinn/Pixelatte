package net.ansinn.pixelatte.formats.png;

public sealed interface FilterResult {

    FilterResult EMPTY = new EmptyResult();

    record BitResult(byte[] bits) implements FilterResult {}
    record ByteResult(byte[] result) implements FilterResult {}
    record IntResult() implements FilterResult {}

    /**
     * Represents an empty FilterResult. Cannot be instantiated and only one instance exists.
     */
    final class EmptyResult implements FilterResult {
        private EmptyResult() {}
    }

}
