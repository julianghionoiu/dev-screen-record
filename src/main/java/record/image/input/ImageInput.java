package record.image.input;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public interface ImageInput {

    //TODO Sort out the exceptions
    void open() throws AWTException, IOException, InterruptedException;

    BufferedImage readImage() throws IOException, InterruptedException;

    /**
     * Provide a sample of the input. This is required to allow the client to initialize the converters ahead of time
     * @return A sample build with the format provided by the input
     */
    BufferedImage getSampleImage();

    int getWidth();

    int getHeight();

    //TODO Sort out exceptions
    void close() throws IOException, InterruptedException;
}
