package net.ansinn.pixelatte.formats.png.layout.chunks;

import net.ansinn.ByteBarista.annotations.UnsignedByte;
import net.ansinn.ByteBarista.annotations.UnsignedShort;
import net.ansinn.pixelatte.formats.png.layout.Chunk;

import java.time.LocalDateTime;



public record tIME(
        @UnsignedShort int year,
        @UnsignedByte short month,
        @UnsignedByte short day,

        @UnsignedByte short hour,
        @UnsignedByte short minute,
        @UnsignedByte short second
) implements Chunk {

    public LocalDateTime toLocalDateTime() {

        return LocalDateTime.of(year, month, day, hour, minute, second);

    }

}
