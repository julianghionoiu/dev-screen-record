package tdl.record.screen.image.input;

import tdl.record.screen.utils.ImageResolution;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class ScaleToCustomSizeImage implements ImageInput {
    private final ImageInput originalInputSource;
    private final int newWidth;
    private final int newHeight;

    private ImageResolution newResolution;
    private BufferedImage targetImage;
    private AffineTransformOp scaleChangeTransform;


    public ScaleToCustomSizeImage(ImageInput originalImageSource, int newWidth, int newHeight) {
        this.originalInputSource = originalImageSource;
        this.newWidth = newWidth;
        this.newHeight = newHeight;
    }

    public static ImageInput scaleImageToValidWidthAndHeight(ImageInput imageInput) throws InputImageGenerationException {
        try {
            imageInput.open();
            int newHeight = scaleToEvenNumberLessThanOrEqualTo(imageInput.getHeight());
            int newWidth = scaleToEvenNumberLessThanOrEqualTo(imageInput.getWidth());
            return new ScaleToCustomSizeImage(imageInput, newWidth, newHeight);
        } catch (InputImageGenerationException e) {
            throw new InputImageGenerationException(e);
        }
    }

    private static int scaleToEvenNumberLessThanOrEqualTo(int value) {
        if (isOdd(value)) {
            return value - 1;
        }
        return value;
    }

    private static boolean isOdd(int value) {
        return (value % 2) != 0;
    }

    @Override
    public void open() throws InputImageGenerationException {
        originalInputSource.open();
        ImageResolution sourceResolution = ImageResolution.of(originalInputSource.getWidth(), originalInputSource.getHeight());
        newResolution = ImageResolution.of(newWidth, newHeight);

        if (sourceResolution.getNumPixels() != newResolution.getNumPixels()) {
            targetImage = new BufferedImage(newResolution.getWidth(), newResolution.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            double sx = newResolution.getWidth() / (double) originalInputSource.getWidth();
            double sy = newResolution.getHeight() / (double) originalInputSource.getHeight();
            AffineTransform at = new AffineTransform();
            at.scale(sx, sy);
            scaleChangeTransform = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
        } else {
            targetImage = new BufferedImage(sourceResolution.getWidth(), sourceResolution.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        }
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
        if (originalImage.getWidth() * originalImage.getHeight() != newResolution.getNumPixels()) {
            scaleChangeTransform.filter(originalImage, targetImage);
        } else {
            targetImage = originalImage;
        }

        return targetImage;
    }

    @Override
    public int getWidth() {
        return targetImage.getWidth();
    }

    @Override
    public int getHeight() {
        return targetImage.getHeight();
    }

    @Override
    public void close() {
        originalInputSource.close();
    }
}