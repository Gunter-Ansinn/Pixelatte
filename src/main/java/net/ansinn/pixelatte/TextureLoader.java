package net.ansinn.pixelatte;

import net.ansinn.pixelatte.formats.png.PNGParser;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class TextureLoader {

    private static final HexFormat format = HexFormat.of();
    private static final Map<byte[], Function<InputStream, IntermediaryImage>> FormatMap = new HashMap<>();

    private TextureLoader() {}


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

    /**
     * Reads a file from a given path and then prints the resulting data alongside how long it took.
     * @param file file to memory map and quickly read
     */
    public static IntermediaryImage readFile(File file) {
        try(var channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            var mbb = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

            

        } catch (IOException exception) {
            System.out.println("Error loading file: " + file.toPath() + " into channel.");
            exception.printStackTrace();
        }

        return new IntermediaryImage();
    }


    static {
        registerFormat(toHex("89 50 4E 47 0D 0A 1A 0A"), PNGParser::parse, CollisionRule.IGNORE);
    }
}
