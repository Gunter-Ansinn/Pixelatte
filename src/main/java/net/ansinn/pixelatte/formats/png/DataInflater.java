package net.ansinn.pixelatte.formats.png;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public final class DataInflater {

    static byte[] inflateData(byte[] dataToInflate) throws DataFormatException {

        var inflater = new Inflater();
        inflater.setInput(dataToInflate);
        var buffer = new byte[1024];
        var outputStream = new ByteArrayOutputStream();

        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        inflater.end();

        return outputStream.toByteArray();
    }
}
