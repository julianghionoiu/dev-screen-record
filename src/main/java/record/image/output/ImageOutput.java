package record.image.output;

import java.awt.image.BufferedImage;

public interface ImageOutput {

    //TODO: Sort out exceptions
    void open() throws IllegalStateException;

    /**
     * Provide a sample of the expected output. This is required to be able to match the format of the BufferedImage.
     *
     * @param width  provided
     * @param height provided
     * @return A sample build with the format required by the output
     */
    BufferedImage getSuggestedOutputSample(int width, int height);

    void writeImage(BufferedImage image);

    void close();
}
