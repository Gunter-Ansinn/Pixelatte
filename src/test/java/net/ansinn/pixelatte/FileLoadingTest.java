package net.ansinn.pixelatte;

import org.junit.jupiter.api.RepeatedTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static net.ansinn.pixelatte.TestUtils.mapRes2File;

class FileLoadingTest {

    static List<String> imagePaths = List.of("basn0g01",
            "basn0g02",
            "basn0g04",
            "basn0g08",
            "basn0g16",
            "basn2c08",
            "basn2c16",
            "basn3p01",
            "basn3p02",
            "basn3p04",
            "basn3p08",
            "basn4a08",
            "basn4a16",
            "basn6a08",
            "basn6a16"
    );

    static int iterCount = 1;

    @RepeatedTest(1)
    void TestLoad() {

        System.out.println("=".repeat(15) + "[" + iterCount++ + "]" + "=".repeat(15));

        imagePaths.forEach(chunk -> mapRes2File("/png_tests/" + chunk + ".png")
                .ifPresent(FileLoadingTest::readFileTimed));

    }

    private static void readFileTimed(File file) {
        var startTime = System.nanoTime();
        TextureLoader.readFile(file);
        var endTime = System.nanoTime();

        System.out.println("Time taken: " + (endTime - startTime) + "ns for: " + file.getName().split("\\|")[0] + " with size of " + getByteMarker(file.length()));
    }

    private static String getByteMarker(long bytes) {
        if (bytes < 0)
            throw new IllegalArgumentException("Bytes contained within a file cannot be less than zero.");

        var units = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB"};
        var index = 0;
        var size = (double) bytes;

        while (size >= 1024 && index < units.length - 1) {
            size /= 1024;
            index++;
        }

        return String.format("%.1f %s", size, units[index]);
    }

}