package net.ansinn.pixelatte.formats.png;

import java.util.zip.Inflater;

public class ParserResources {

    public final Inflater inflater = new Inflater();
    public final byte[] scratch = new byte[8192]; // About 8kb should be appropriate
    public final byte[] headerSkip = new byte[8];

}
