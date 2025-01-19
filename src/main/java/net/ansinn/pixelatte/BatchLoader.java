package net.ansinn.pixelatte;

/**
 * Load Images in a batch from a file directory.
 */
public class BatchLoader {

    String fileDir;

    private BatchLoader() {};

    public static BatchLoader create() {
        return new BatchLoader();
    }

    /**
     * Create a batch loader and set
     * @param fileDir
     * @return
     */
    public static BatchLoader of(String fileDir) {
        return new BatchLoader();
    }

}
