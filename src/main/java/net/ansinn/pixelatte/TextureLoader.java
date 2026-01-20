package net.ansinn.pixelatte;

import net.ansinn.pixelatte.formats.png.PNGParser;
import net.ansinn.pixelatte.output.DecodedImage;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;

/**
 * A utility class designed to handle image parsing for files of unknown types.
 * This class facilitates the dynamic dispatching of parsing responsibilities
 * to appropriate format-specific parsers based on image signature detection.
 * <p>
 * The `TextureLoader` maintains a registry of supported formats, allowing developers
 * to register new image parsers by associating file magic numbers with processing functions.
 * It enforces intentional collision handling when replacing parsers, ensuring robust customization.
 * <p>
 * Key Features:
 * - Dynamic format registration and dispatching.
 * - Support for extensibility with user-defined parsers.
 * - Focus on decoupling image format detection from actual parsing logic.
 * <p>
 * This class operates with a static registry and is not intended to be instantiated.
 *
 * @author Gunter Paul Ansinn
 * @see IntermediaryImage
 */
public final class TextureLoader {

    private static final HexFormat format = HexFormat.of();

    // Replaces the Map with a specialized Trie root
    private static final ByteTrieNode ROOT = new ByteTrieNode();

    private TextureLoader() {}

    /**
     * Specialized Trie Node for byte-based lookups.
     * Uses simple arrays (SoA - Structure of Arrays style) instead of Maps to keep nodes lightweight and cache-friendly.
     * Since magic number branching is low (usually < 5 branches per byte), a linear scan over 
     * a tiny array is faster than a HashMap lookup.
     */
    private static class ByteTrieNode {
        byte[] keys = new byte[0];
        ByteTrieNode[] children = new ByteTrieNode[0];
        Function<ByteBuffer, DecodedImage> parser;

        void add(byte[] signature, int index, Function<ByteBuffer, DecodedImage> parser, CollisionRule rule) {
            // Base case: We've consumed the entire signature
            if (index == signature.length) {
                if (this.parser != null && rule == CollisionRule.IGNORE) {
                    return; // Collision: Ignore new parser
                }
                this.parser = parser; // Set/Overwrite parser
                return;
            }

            byte currentByte = signature[index];
            int childIndex = -1;

            // Linear scan to find if we already have a path for this byte
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] == currentByte) {
                    childIndex = i;
                    break;
                }
            }

            // If path doesn't exist, extend the arrays (Copy-on-Write style)
            if (childIndex == -1) {
                childIndex = keys.length;
                keys = Arrays.copyOf(keys, keys.length + 1);
                children = Arrays.copyOf(children, children.length + 1);
                
                keys[childIndex] = currentByte;
                children[childIndex] = new ByteTrieNode();
            }

            // Recurse down
            children[childIndex].add(signature, index + 1, parser, rule);
        }

        Function<ByteBuffer, DecodedImage> find(ByteBuffer buffer) {
            ByteTrieNode currentNode = this;
            Function<ByteBuffer, DecodedImage> lastValidParser = null;
            
            // We walk the buffer without modifying its position
            for (int i = 0; i < buffer.remaining(); i++) {
                byte b = buffer.get(i);
                int nextIndex = -1;

                // Find child for byte 'b'
                for (int k = 0; k < currentNode.keys.length; k++) {
                    if (currentNode.keys[k] == b) {
                        nextIndex = k;
                        break;
                    }
                }

                if (nextIndex == -1) {
                    // Dead end. Return the deepest parser we found along the way.
                    // This handles cases where a file might have extra data after the signature
                    // but matched a valid shorter signature.
                    return lastValidParser;
                }

                // Move down
                currentNode = currentNode.children[nextIndex];

                // If this node is a valid endpoint, remember it
                if (currentNode.parser != null) {
                    lastValidParser = currentNode.parser;
                }
            }
            
            return lastValidParser;
        }
    }


    /**
     * Register a parsable image format and its according image processing function. Explicit collision rules must be
     * specified as a way of opting into intentional replacement of a parser.
     * @param magicNumber    the identifying magic number of an image format.
     * @param imageProcessor the function in charge of processing an image.
     */
    public static void registerFormat(String magicNumber, Function<ByteBuffer, DecodedImage> imageProcessor, CollisionRule collisionRule) {
        var magicNumberKey = toHex(magicNumber);
        ROOT.add(magicNumberKey, 0, imageProcessor, collisionRule);
    }

    /**
     * Simple shorthand function to convert a string of numbers to an array of bytes.
     */
    private static byte[] toHex(String hex) {
        return format.parseHex(hex.replace(" ",""));
    }

    /**
     * Reads an image from a memory mapped file, then determines what the image format is, and then parses the image
     * based on type. The returned image is given within the intermediary format for additional data processing.
     *
     * @param file file to memory map and quickly read
     * @return intermediary representation of an image
     */
    public static DecodedImage readFile(File file) {

        try(var channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            var mbb = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

            // 0 Allocations just walk.
            var parser = ROOT.find(mbb);
            
            if (parser != null) {
                return parser.apply(mbb);
            }

        } catch (IOException exception) {
            System.out.println("Error loading file: " + file.toPath() + " into channel.");
            exception.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException("uhh", e);
        }

        return null;
    }

    static DecodedImage empty(ByteBuffer stream) {
        System.out.println("Not yet implemented.");
        return null;
    }

    static {
        registerFormat("89 50 4E 47 0D 0A 1A 0A", PNGParser::parse, CollisionRule.IGNORE); //PNG
        registerFormat("FF D8 FF", TextureLoader::empty, CollisionRule.IGNORE); //JPEG

        registerFormat("47 49 46 38 37 61", TextureLoader::empty, CollisionRule.IGNORE); // GIF variant 1
        registerFormat("47 49 46 38 39 61", TextureLoader::empty, CollisionRule.IGNORE); // GIF variant 2

        registerFormat("42 4D", TextureLoader::empty, CollisionRule.IGNORE); // BMP

        registerFormat("49 49 2A 00", TextureLoader::empty, CollisionRule.IGNORE); // TIFF (Little Endian)
        registerFormat("4D 4D 00 2A", TextureLoader::empty, CollisionRule.IGNORE); // TIFF (Big Endian)

    }
}