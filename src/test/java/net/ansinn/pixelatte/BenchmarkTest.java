package net.ansinn.pixelatte;

import net.ansinn.pixelatte.formats.png.PNGParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BenchmarkTest {

    private record TestImage(String name, byte[] data) {}

    private static final List<String> IMAGE_NAMES = List.of(
            "basn0g01", "basn0g02", "basn0g04", "basn0g08", "basn0g16",
            "basn2c08", "basn2c16",
            "basn3p01", "basn3p02", "basn3p04", "basn3p08",
            "basn4a08", "basn4a16",
            "basn6a08", "basn6a16"
    );

    @Test
    void benchmarkParsers() throws IOException {
        System.out.println("Preparing Benchmark...");
        List<TestImage> images = loadImages();
        System.out.println("Loaded " + images.size() + " images into memory.");

        // Warmup
        System.out.println("Warming up JVM...");
        for (int i = 0; i < 50; i++) {
            runPixelatte(images);
            runImageIO(images);
        }

        // Benchmark Pixelatte
        System.out.println("Benchmarking Pixelatte...");
        long startPixelatte = System.nanoTime();
        int iterations = 100;
        for (int i = 0; i < iterations; i++) {
            runPixelatte(images);
        }
        long endPixelatte = System.nanoTime();
        double avgPixelatte = (endPixelatte - startPixelatte) / (double) iterations / 1_000_000.0;

        // Benchmark ImageIO
        System.out.println("Benchmarking ImageIO...");
        long startImageIO = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            runImageIO(images);
        }
        long endImageIO = System.nanoTime();
        double avgImageIO = (endImageIO - startImageIO) / (double) iterations / 1_000_000.0;

        System.out.println("\n=== Results (Average time to parse all " + images.size() + " images) ===");
        System.out.printf("Pixelatte: %.4f ms\n", avgPixelatte);
        System.out.printf("ImageIO:   %.4f ms\n", avgImageIO);
        double diff = avgImageIO / avgPixelatte;
        System.out.printf("Speedup:   %.2fx (Pixelatte is %s)\n", diff, diff > 1 ? "faster" : "slower");
    }

    private List<TestImage> loadImages() throws IOException {
        List<TestImage> images = new ArrayList<>();
        for (String name : IMAGE_NAMES) {
            String path = "/png_tests/" + name + ".png";
            try (var stream = getClass().getResourceAsStream(path)) {
                if (stream == null) {
                    System.err.println("Could not find resource: " + path);
                    continue;
                }
                images.add(new TestImage(name, stream.readAllBytes()));
            }
        }
        return images;
    }

    private void runPixelatte(List<TestImage> images) {
        for (TestImage img : images) {
            PNGParser.parse(ByteBuffer.wrap(img.data()));
        }
    }

    private void runImageIO(List<TestImage> images) {
        for (TestImage img : images) {
            try {
                ImageIO.read(new ByteArrayInputStream(img.data()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
