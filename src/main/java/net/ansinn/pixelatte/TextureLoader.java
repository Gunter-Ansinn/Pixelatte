package net.ansinn.pixelatte;

import net.ansinn.pixelatte.formats.png.PNGParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.function.Function;

public final class TextureLoader {

    private static final HexFormat format = HexFormat.of();
    private static final Map<byte[], Function<InputStream, IntermediaryImage>> FormatMap = new HashMap<>();

    private TextureLoader() {}

    /**
     * Standard image parsing function. Given any unspecified file it will try to parse out what it is and what image format
     * it belongs to. However, if you have a guarantee of what image formats you're feeding to these methods you may
     * instead refer to the individual pre-made functions instead.
     * <p></p>
     * <p>[!] This method assumes you're starting at the beginning of an InputStream</p>
     * @param input the data input stream containing the image
     * @return an intermediary representation of an image. This can be converted into different types manually for
     * dealing with libraries.
     * @throws IOException
     */
    public static IntermediaryImage LoadImage(InputStream input) throws IOException {
        // Check if formats list is empty, if it is throw an error because it shouldn't be.
        if (FormatMap.isEmpty())
            throw new IllegalStateException("No formats exist within the image format registry.");

        var reading = true;
        var availableNums = FormatMap.keySet();

        // TODO complete this later.

        return null;
    }

    /**
     * Register a parsable image format and its according image processing function. Explicit collision rules must be
     * specified as a way of opting into intentional replacement of a parser.
     * @param magicNumber the identifying magic number of an image format.
     * @param imageProcessor the function in charge of processing an image.
     */
    public static void registerFormat(byte[] magicNumber, Function<InputStream, IntermediaryImage> imageProcessor, CollisionRule collisionRule) {
        switch (collisionRule) {
            case IGNORE -> FormatMap.putIfAbsent(magicNumber, imageProcessor);
            case OVERWRITE -> FormatMap.put(magicNumber, imageProcessor);
        }
    }

    /**
     * Simple shorthand function to convert a string of numbers to an array of bytes.
     */
    private static byte[] toHex(String hex) {
        return format.parseHex(hex.replace(" ",""));
    }

    static {
        registerFormat(toHex("89 50 4E 47 0D 0A 1A 0A"), PNGParser::parse, CollisionRule.IGNORE);
    }
}
