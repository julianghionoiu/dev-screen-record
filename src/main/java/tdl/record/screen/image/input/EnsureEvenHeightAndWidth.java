 package tdl.record.screen.image.input;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class EnsureEvenHeightAndWidth implements ImageInput {
    private final ImageInput originalInputSource;


    public EnsureEvenHeightAndWidth(ImageInput originalImageSource) {
        this.originalInputSource = originalImageSource;
    }

    @Override
    public void open() throws InputImageGenerationException {
        originalInputSource.open();
    }

    @Override
    public BufferedImage readImage() throws InputImageGenerationException {
        return processImage(originalInputSource.readImage());
    }

    @Override
    public BufferedImage getSampleImage() throws InputImageGenerationException {
        return processImage(originalInputSource.getSampleImage());
    }

    private BufferedImage processImage(BufferedImage originalImage) {
        if (needsProcessing(originalImage)) {
            int width = ensureEven(originalImage.getWidth());
            int height = ensureEven(originalImage.getHeight());
            Raster data = originalImage.getData(new Rectangle(0, 0, width, height));

            WritableRaster writableRaster = data.createCompatibleWritableRaster();
            writableRaster.setDataElements(0, 0, data);
            return new BufferedImage(
                    originalImage.getColorModel(),
                    writableRaster,
                    originalImage.isAlphaPremultiplied(),
                    null);
        } else {
            return originalImage;
        }
    }

    private static boolean needsProcessing(BufferedImage originalImage) {
        return isOdd(originalImage.getHeight()) || isOdd(originalImage.getWidth());
    }

    @Override
    public int getWidth() {
        return ensureEven(originalInputSource.getWidth());
    }

    @Override
    public int getHeight() {
        return ensureEven(originalInputSource.getHeight());
    }

    private static int ensureEven(int length) {
        return isOdd(length) ? length - 1 : length;
    }

    private static boolean isOdd(int length) {
        return length % 2 == 1;
    }

    @Override
    public void close() {
        originalInputSource.close();
    }
}
