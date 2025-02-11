package net.ansinn.pixelatte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

public class TestUtils {

    public static Optional<File> mapRes2File(String path) {
        try(var resource = Main.class.getResourceAsStream(path)) {
            if (resource == null)
                throw new FileNotFoundException("Stream could not be created");

            var temp = File.createTempFile(path.split("/")[2] + " | ",".tmp");

            temp.deleteOnExit();

            try(var out = new FileOutputStream(temp)) {
                resource.transferTo(out);
            }

            return Optional.of(temp);
        } catch (IOException exception) {
            System.out.println("You suck at this.");
            exception.printStackTrace();
        }

        return Optional.empty();
    }

}
