package record.image.input;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface ImageInput {

    //TODO Sort out the exceptions
    void open() throws InputImageGenerationException;

    BufferedImage readImage()
        throws InputImageGenerationException;

    /**
     * Provide a sample of the input. This is required to allow the client to initialize the converters ahead of time
     * @return A sample build with the format provided by the input
     */
    BufferedImage getSampleImage() throws InputImageGenerationException;

    int getWidth();

    int getHeight();

    //TODO Sort out exceptions
    void close() throws IOException, InterruptedException;
}
